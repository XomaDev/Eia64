//
// Created by kumaraswamy on 8/16/24.
//

#ifndef VM_ASSEMBLER_H
#define VM_ASSEMBLER_H


#include <string>
#include "bytecode_writer.h"

class assembler {
    const std::string &source_path;
    bytecode_writer writer;

    void begin();

public:
    explicit assembler(const std::string &source_path, const std::string &compiled_path)
            : source_path(source_path), writer(compiled_path) {
        begin();
    }
};


#endif //VM_ASSEMBLER_H
