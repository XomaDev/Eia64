//
// Created by kumaraswamy on 8/16/24.
//

#include "bytecode_writer.h"

void bytecode_writer::writeByte(uint8_t value) {
    sink.write(reinterpret_cast<char *>(&value), 1);
}

void bytecode_writer::write(bytecode code) {
    writeByte(static_cast<uint8_t>(code));
}

void bytecode_writer::writeInt32(uint64_t number) {
    writeByte(number >> 24);
    writeByte(number >> 16);
    writeByte(number >> 8);
    writeByte(number);
}

void bytecode_writer::writeString(const std::string &content) {
    auto str_length = content.size();
    if (str_length > 255) {
        throw std::runtime_error("String length cannot exceed 255 characters");
    }
    writeByte(static_cast<uint8_t>(str_length));
    sink.write(content.data(), str_length);
}

void bytecode_writer::close() {
    sink.close();
}