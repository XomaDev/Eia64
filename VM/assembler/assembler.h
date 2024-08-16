//
// Created by kumaraswamy on 8/16/24.
//

#ifndef VM_ASSEMBLER_H
#define VM_ASSEMBLER_H


#include <string>
#include "bytecode_writer.h"

class assembler {
    std::ifstream source;
    bytecode_writer writer;

    void begin();

public:
    explicit assembler(const std::string &source_path, const std::string &compiled_path)
            : source(source_path), writer(compiled_path) {
        if (!source.is_open()) {
            std::cerr << "Unable to open source path " + source_path << std::endl;
            return;
        }
        begin();
    }

    void readScope();
};


#endif //VM_ASSEMBLER_H
