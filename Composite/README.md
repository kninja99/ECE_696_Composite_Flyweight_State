# Composite AST — Arithmetic Expression Parser & Evaluator

An ECE 696 design-patterns assignment. This project parses arithmetic expressions
into an **Abstract Syntax Tree built with the Composite design pattern**, evaluates
the tree by traversal, prints it, and validates results against a **CSV test
oracle** using JUnit 5.

## What it does

1. **Build an AST (Composite pattern).** A recursive-descent parser turns text
   like `2 + 3 * 4` into a tree of `Expression` nodes.
2. **Evaluate by traversal.** Each node computes its own value from its children.
3. **Print & test against an oracle.** Tests parse each expression, evaluate it,
   and compare with the expected value listed in `src/test/resources/expressions.csv`.
4. **Evaluate many expressions.** The `Main` demo runs a sample set (or your own).

## Supported grammar

```
expression := term      (('+' | '-') term)*
term       := factor    (('*' | '/' | '%') factor)*
factor     := '-' factor | power
power      := primary    ('^' factor)?        // right-associative
primary    := NUMBER | '(' expression ')'
```

* Operators: `+  -  *  /  %  ^`, parentheses, and unary minus.
* `^` is **right-associative** and binds tighter than unary minus, so
  `-3^2 = -9`, `2^3^2 = 512`, and `2^-1 = 0.5`.
* All arithmetic is `double`. Division by zero follows IEEE-754 (`Infinity`/`NaN`),
  and `%` is Java's truncated remainder.

## Composite pattern roles

| Role                  | Class(es) |
|-----------------------|-----------|
| Component (interface) | `Expression` |
| Leaf                  | `NumberExpression` |
| Composite (unary)     | `NegateExpression` |
| Composite (binary)    | `BinaryExpression` → `Add`, `Subtract`, `Multiply`, `Divide`, `Modulo`, `Power` |

The front end lives in `edu.arizona.ece696.ast.parser` (`Lexer`, `Parser`,
`Token`, `TokenType`, `ParseException`).

## Project layout

```
Composite/
├── pom.xml
├── src/main/java/edu/arizona/ece696/ast/         # AST nodes + Main demo
│   └── parser/                                    # Lexer + Parser
└── src/test/
    ├── java/edu/arizona/ece696/ast/              # JUnit 5 tests
    └── resources/expressions.csv                 # the test oracle
```

## Build & run

### Eclipse (no command-line Maven needed)

1. **File ▸ Import… ▸ Maven ▸ Existing Maven Projects**, select the `Composite`
   folder, Finish. Eclipse's bundled Maven (m2e) resolves JUnit on first build
   (needs internet once).
2. Run the demo: right-click `Main.java` ▸ **Run As ▸ Java Application**.
3. Run the tests: right-click the `src/test/java` folder ▸ **Run As ▸ JUnit Test**.

### Command line (if Maven is installed)

```bash
mvn test                                              # run all JUnit tests
mvn compile exec:java                                 # run the Main demo
mvn compile exec:java -Dexec.args="2+3*4 -3^2"        # evaluate your own expressions
```

### Quick smoke test with only a JDK (no Maven)

From the `Composite` folder:

```bash
find src/main/java -name "*.java" > sources.txt
javac -d out @sources.txt
java -cp out edu.arizona.ece696.ast.Main
```

## The test oracle

`src/test/resources/expressions.csv` holds `expression,expected` rows (with `#`
comment lines). `ExpressionEvaluatorTest` reads it via `@MethodSource`, parses and
evaluates each expression, and asserts the result matches the expected value within
`1e-9`. **Add a new test case by adding a line to the CSV** — no code change needed.

## Sample output

```
Expression : -3 ^ 2
AST        :
    \-- - (unary)
        \-- ^
            |-- 3.0
            \-- 2.0
Infix form : (-(3.0 ^ 2.0))
Value      : -9.0
```
