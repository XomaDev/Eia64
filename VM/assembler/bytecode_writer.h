//
// Created by kumaraswamy on 8/16/24.
//

#ifndef VM_BYTECODE_WRITER_H
#define VM_BYTECODE_WRITER_H


#include <fstream>
#include <iostream>
#include <cstdint>
#include <vector>
#include "../bytecode.h"

class bytecode_writer {
public:
    std::vector<uint8_t> sink;

    void writeByte(uint8_t value);
    void write(bytecode code);
    void writeInt32(uint64_t);
    void writeString(const std::string &content);
    void clear();
};


#endif //VM_BYTECODE_WRITER_H
