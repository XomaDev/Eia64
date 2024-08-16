//
// Created by kumaraswamy on 8/16/24.
//

#include <fstream>
#include <iostream>
#include "assembler.h"

void assembler::begin() {
    std::ifstream file(file_path);

    if (!file.is_open()) {
        std::cerr << "Unable to open file path " + file_path << std::endl;
        return;
    }

    std::string word;
    while (file >> word) {
        std::cout << word << std::endl;
    }
    file.close();
}