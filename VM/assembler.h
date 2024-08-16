//
// Created by kumaraswamy on 8/16/24.
//

#ifndef VM_ASSEMBLER_H
#define VM_ASSEMBLER_H


#include <string>

class assembler {
    const std::string& file_path;

    void begin();
public:
    explicit assembler(const std::string& path): file_path(path) {
        // Meow
        begin();
    }
};


#endif //VM_ASSEMBLER_H
