[![Watch the video](https://raw.githubusercontent.com/XomaDev/Eia64/readme/graphics/PlayThumb.png)](https://youtu.be/H1s11JVKE2M)


# A New Programming Language — Eia64


![EiaPromo.png](https://raw.githubusercontent.com/XomaDev/Eia64/readme/graphics/EiaPromo.png)

SSH into `hackclub.app`

````shell
ssh hackclub.app -p 2121
````
Or try Eia-Playground — [eia.themelon.space]()

## Highlights ✨

- Supports <b>`String`, `Int`, `Bool`, `Float`, `Array`, `List`</b>
- **Functions**, **Variables**, **Lambdas** ✌️
- **Object-oriented**: Creating **Classes** and **Objects** ☕
- Automatic type resolution — who specifies variable types nowadays? (maybe Java does)
- Comes with a **Standard Library** 📚
- Debug your code with **Live Tracer** 💪


## How he works

Eia64 is an interpreted language ✨\
There are 3 main stages
1. Lexing — first level of syntax breakdown\
   A common process among all the languages.\
   It splits the code, identifies and marks each token into categories such as Operators, Literals (Int/String value), Function Calls, etc. This metadata produced helps in the next stage i.e. parsing.
     
2. Parsing\
  In computer systems, parsing refers to organizing data into a structured way in which it can be operated further.\
  The parser receives an array of Lexed tokens with metadata and loops over it to form Abstract Syntax Trees (AST).
  Just like how we can choose many paths in life, the tree guides the execution flow of a program.

An example for lexing, parsing and execution of code <b>`print(1 + 2 * 3)`</b>

![Step1.png](https://raw.githubusercontent.com/XomaDev/Eia64/readme/graphics/Step1.png)

In the above demonstration, after taking in the code input, lexer emits a stream or an array of tokens. After the Parser receives it, it converts it into a Tree.\
An expression can contain many sub expressions, for e.g. `+{Int{2}, Int{3}}`. 

![Step2.png](https://raw.githubusercontent.com/XomaDev/Eia64/readme/graphics/Step2.png)

3. Execution\
  In interpreters, execution is the last step. If it was for compilers, this step would be compilation, where bytecodes or machine codes are emitted.\
  Consider there is an expression <b>`X`</b> and it contains sub-expressions <b>`Y`</b> and `Z`</b>. Here, <b>`X`</b>'s value is dependent on sub-expressions `Y` and `Z`. So <b>`Y`</b> and <b>`Z`</b> should be first fully evaluated before `X` is evaluated.\
  This is repeated until the final node gets evaluated.

Thank You
