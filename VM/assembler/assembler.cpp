//
// Created by kumaraswamy on 8/16/24.
//

#include <limits>
#include <iostream>
#include "assembler.h"

void assembler::begin() {
    std::string scopeName;
    while (source >> scopeName) {
        writer.writeString(scopeName);
        readScope();
        writer.write(bytecode::SCOPE_END);
    }
    source.close();
    writer.close();
}

void assembler::readScope() {
    std::string word;
    while (source >> word) {
        if (word == "#") {
            source.ignore(std::numeric_limits<std::streamsize>::max(), '\n');
        } else if (word == "Bool") {
          writer.write(bytecode::BOOL);
          source >> word;

          if (word == "True") writer.writeByte(1);
          else if (word == "False") writer.writeByte(0);
          else throw std::runtime_error("Bad bool value " + word);

        } else if (word == "Int") {
            writer.write(bytecode::INT);
            source >> word;
            writer.writeInt32(stoi(word));
        } else if (word == "Str") {
            writer.write(bytecode::STRING);
            if (source.get() != ' ') throw std::runtime_error("Expected space after 'str'");
            if (source.get() != '\"') throw std::runtime_error("Expected colon after 'str'");
            std::string content;
            for (;;) {
                char ch;
                source.get(ch);
                if (ch == '\"') break;
                content += ch;
            }
            writer.writeString(content);
        }
        // TODO:
        //  We gotta add features to convert Numbers into Strings
        else if (word == "Add") writer.write(bytecode::ADD);
        else if (word == "Add_Str") writer.write(bytecode::ADD_STR);
        else if (word == "Sub") writer.write(bytecode::SUB);
        else if (word == "Mul") writer.write(bytecode::MUL);
        else if (word == "Div") writer.write(bytecode::DIV);

        else if (word == "Neg") writer.write(bytecode::NEG);
        else if (word == "Not") writer.write(bytecode::NOT);

        else if (word == "Read") writer.write(bytecode::READ);
        else if (word == "Read_Ln") writer.write(bytecode::READ_LN);
        else if (word == "Print") writer.write(bytecode::PRINT);
        else if (word == "Print_Str") writer.write(bytecode::PRINT_STR);
        else if (word == "Endl") writer.write(bytecode::END_LINE);
        else if (word == "Halt") writer.write(bytecode::HALT);
        else if (word == "To_Str") writer.write(bytecode::TO_STR);
        else if (word == "Str_Len") writer.write(bytecode::STR_LEN);
        else if (word == "Pop_Str") writer.write(bytecode::POP_STR);
        else if (word == "Store") {
            writer.write(bytecode::STORE);
            source >> word;
            writer.writeInt32(stoi(word));
        } else if (word == "Load") {
            writer.write(bytecode::LOAD);
            source >> word;
            writer.writeInt32(stoi(word));
        }

        else if (word == "Int_Cmp") writer.write(bytecode::INT_CMP);
        else if (word == "Str_Cmp") writer.write(bytecode::STR_CMP);

        else if (word == "Go") {
            // goes to other named scope
            writer.write(bytecode::GO);
            source >> word; // scope name
            writer.writeString(word);
        } else if (word == "Geq") {
            /* go if equal */
            writer.write(bytecode::GO_EQUAL);
            source >> word; // scope name
            writer.writeString(word);
        } else if (word == "Gnq") {
            /* go if not equal */
            writer.write(bytecode::GO_UNEQUAL);
            source >> word; // scope name
            writer.writeString(word);
        }

        else if (word == "end") break; // the scope ends here

        else throw std::runtime_error("Unknown instruction " + word);
    }
}