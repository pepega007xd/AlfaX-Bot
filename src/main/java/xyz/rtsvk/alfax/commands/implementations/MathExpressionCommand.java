package xyz.rtsvk.alfax.commands.implementations;

import discord4j.common.util.Snowflake;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.entity.User;
import discord4j.core.object.entity.channel.MessageChannel;
import xyz.rtsvk.alfax.commands.Command;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.stream.Collectors;

public class MathExpressionCommand implements Command {

	private final List<Symbol> symbols = new ArrayList<>();

	public MathExpressionCommand() {
		this.symbols.add(new Symbol("pi", Math.PI));
		this.symbols.add(new Symbol("e", Math.E));
		this.symbols.add(new Symbol("phi", 1.61803398874989484820458683436563811772030917980576286213544862270526046281890));
		this.symbols.add(new Symbol("ln2", Math.log(2)));
		// TODO: add sqrt, sin, cos, tan, etc.
	}

	@Override
	public void handle(User user, MessageChannel channel, List<String> args, Snowflake guildId, GatewayDiscordClient bot) throws Exception {
		String rawExpression = args.stream().filter(s -> !s.contains("=")).collect(Collectors.joining(""));
		Stack<Token> stack = new Stack<>();
		Stack<Double> evalStack = new Stack<>();
		List<Token> tokens = tokenize(rawExpression);
		List<Token> postfix = new ArrayList<>();
		List<Symbol> symtable = new ArrayList<>(this.symbols);

		args = args.stream().filter(s -> !s.isEmpty()).collect(Collectors.toList());
		args.stream().filter(s -> s.contains("=")).forEach(s -> {
			String[] parts = s.split("=");

			String name = parts[0];
			String value = parts[1];
			if (this.symbols.stream().anyMatch(symbol -> symbol.getName().equals(name))) {
				channel.createMessage("Symbol '" + name + "' already exists").block();
				return;
			}
			try {
				double val = Double.parseDouble(value);
				symtable.add(new Symbol(name, val));
			} catch (NumberFormatException e) {
				channel.createMessage("Invalid symbol value `" + value + "`").block();
			}
		});

		// convert the expression to postfix notation
		for (Token token : tokens) {
			if (token.type == TokenType.INTEGER_LITERAL || token.type == TokenType.DOUBLE_LITERAL || token.type == TokenType.SYMBOL) {
				postfix.add(token);
			}
			else if (token.type == TokenType.OPERATOR) {
				while (!stack.empty() && stack.peek().type == TokenType.OPERATOR && getPriority(stack.peek().value.charAt(0)) >= getPriority(token.value.charAt(0))) {
					postfix.add(stack.pop());
				}
				stack.push(token);
			}
			else if (token.type == TokenType.OPEN_BRACKET) {
				stack.push(token);
			}
			else if (token.type == TokenType.CLOSE_BRACKET) {
				while (!stack.empty() && stack.peek().type != TokenType.OPEN_BRACKET) {
					postfix.add(stack.pop());
				}
				stack.pop();
			}
		}

		while (!stack.empty()) {
			postfix.add(stack.pop());
		}

		// evaluate the expression
		for (Token token : postfix) {
			if (token.getType() == TokenType.INTEGER_LITERAL || token.getType() == TokenType.DOUBLE_LITERAL) {
				evalStack.push(Double.parseDouble(token.getValue()));
			}
			else if (token.getType() == TokenType.SYMBOL) {
				Symbol symbol = symtable.stream().filter(s -> s.getName().equals(token.getValue())).findFirst().orElse(null);
				if (symbol == null) {
					channel.createMessage("Unknown symbol `" + token.getValue() + "`").block();
					return;
				}
				evalStack.push((Double) symbol.getValue());
			}
			else if (token.getType() == TokenType.OPERATOR) {
				double b = evalStack.pop();
				double a = evalStack.pop();
				switch (token.getValue()) {
					case "+":
						evalStack.push(a + b);
						break;
					case "-":
						evalStack.push(a - b);
						break;
					case "×":
					case "*":
						evalStack.push(a * b);
						break;
					case "÷":
					case "/":
						if (b == 0.0) {
							channel.createMessage("Division by zero").block();
							return;
						}
						evalStack.push(a / b);
						break;
					case "%":
						evalStack.push(a % b);
						break;
					case "^":
						evalStack.push(Math.pow(a, b));
						break;
					default:
						channel.createMessage("Unknown operator `" + token.value + "`").block();
						return;
				}
			}
		}

		channel.createMessage("```\n" + rawExpression  + " = " + evalStack.pop() + "\n```").block();
	}

	private List<Token> tokenize(String expression) {   // lexer (tokenizer)
		List<Token> tokens = new ArrayList<>();
		StringBuilder token = new StringBuilder();

		int index = 0;
		while (index < expression.length()) {
			if (isOperator(expression.charAt(index))) {
				if (token.length() > 0) {
					tokens.add(new Token(token.toString(), TokenType.SYMBOL));
					token = new StringBuilder();
				}
				tokens.add(new Token(String.valueOf(expression.charAt(index)), TokenType.OPERATOR));
				index++;
			}
			else if (expression.charAt(index) == '(') {
				if (token.length() > 0) {
					tokens.add(new Token(token.toString(), TokenType.SYMBOL));
					token = new StringBuilder();
				}
				tokens.add(new Token(String.valueOf(expression.charAt(index)), TokenType.OPEN_BRACKET));
				index++;
			}
			else if (expression.charAt(index) == ')') {
				if (token.length() > 0) {
					tokens.add(new Token(token.toString(), TokenType.SYMBOL));
					token = new StringBuilder();
				}
				tokens.add(new Token(String.valueOf(expression.charAt(index)), TokenType.CLOSE_BRACKET));
				index++;
			}
			else if (Character.isDigit(expression.charAt(index))) {
				while (index < expression.length() && (Character.isDigit(expression.charAt(index)) || expression.charAt(index) == '.')) {
					token.append(expression.charAt(index));
					index++;
				}
				tokens.add(new Token(token.toString(), token.toString().contains(".") ? TokenType.DOUBLE_LITERAL : TokenType.INTEGER_LITERAL));
				token = new StringBuilder();
			}
			else {
				token.append(expression.charAt(index));
				index++;
			}
		}

		if (token.length() > 0) {
			tokens.add(new Token(token.toString(), TokenType.SYMBOL));
		}

		return tokens;
	}

	private boolean isOperator(char c) {
		return List.of('+', '-', '×', '*', '÷', '/', '%', '^').contains(c);
	}

	private int getPriority(char c) {
		switch (c) {
			case '+':
			case '-':
				return 1;
			case '*':
			case '/':
			case '%':
			case '×':
			case '÷':
				return 2;
			case '^':
				return 3;
			default:
				return 0;
		}
	}

	@Override
	public String getName() {
		return "calculate";
	}

	@Override
	public String getDescription() {
		return "Calculate a math expression";
	}

	@Override
	public String getUsage() {
		return "calculate <expression>";
	}

	@Override
	public List<String> getAliases() {
		return List.of("calc", "math");
	}

	private static class Token {
		private final String value;
		private final TokenType type;

		public Token(String value, TokenType type) {
			this.value = value;
			this.type = type;
		}

		public String getValue() {
			return value;
		}

		public TokenType getType() {
			return type;
		}
	}

	private enum TokenType {
		DOUBLE_LITERAL,
		INTEGER_LITERAL,
		SYMBOL,
		OPERATOR,
		OPEN_BRACKET,
		CLOSE_BRACKET
	}

	private static class Symbol {
		private final String name;
		private final Object value;
		private final boolean isFunction;

		public Symbol(String name, Object value) {
			this(name, value, false);
		}

		public Symbol(String name, Object value, boolean isFunction) {
			this.name = name;
			this.value = value;
			this.isFunction = isFunction;
		}

		public String getName() {
			return name;
		}

		public Object getValue() {
			return value;
		}

		public boolean isFunction() {
			return isFunction;
		}
	}
}
