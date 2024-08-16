//
// Created by kumaraswamy on 8/16/24.
//

#include <fstream>
#include <iostream>
#include "assembler.h"

void assembler::begin() {
    std::ifstream source(source_path);

    if (!source.is_open()) {
        std::cerr << "Unable to open source path " + source_path << std::endl;
        return;
    }

    std::string word;
    while (source >> word) {
        if (word == "bool") {
          writer.write(bytecode::BOOL);
          source >> word;

          if (word == "true") writer.writeByte(1);
          else if (word == "false") writer.writeByte(0);
          else throw std::runtime_error("Bad bool value " + word);

        } else if (word == "int") {
            writer.write(bytecode::INT);
            source >> word;
            writer.writeInt32(stoi(word));
        } else if (word == "str") {
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
        else if (word == "add") writer.write(bytecode::ADD);
        else if (word == "add_str") writer.write(bytecode::ADD_STR);
        else if (word == "sub") writer.write(bytecode::SUB);
        else if (word == "mul") writer.write(bytecode::MUL);
        else if (word == "div") writer.write(bytecode::DIV);

        else if (word == "print") writer.write(bytecode::PRINT);
        else if (word == "print_str") writer.write(bytecode::PRINT_STR);
        else if (word == "endl") writer.write(bytecode::END_LINE);

        else if (word == "halt") writer.write(bytecode::HALT);

        else throw std::runtime_error("Unknown instruction " + word);
    }
    source.close();
    writer.close();
}