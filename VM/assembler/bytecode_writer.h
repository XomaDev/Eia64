//
// Created by kumaraswamy on 8/16/24.
//

#ifndef VM_BYTECODE_WRITER_H
#define VM_BYTECODE_WRITER_H


#include <fstream>
#include <iostream>
#include <cstdint>
#include "../bytecode.h"

class bytecode_writer {
    std::ofstream sink;
public:
    explicit bytecode_writer(const std::string &output_path) {
        sink = std::ofstream(output_path, std::ios::binary);
        if (!sink.is_open()) {
            std::cerr << "Unable to open file to write " + output_path << std::endl;
        }
    }

    void writeByte(uint8_t value);
    void write(bytecode code);
    void writeInt32(uint64_t);
    void writeString(const std::string &content);
    void close();
};


#endif //VM_BYTECODE_WRITER_H
