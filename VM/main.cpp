#include <iostream>
#include <memory>
#include <fstream>

#include "vm.h"
#include "assembler/assembler.h"

std::unique_ptr<uint8_t[]> readBytes(const std::string& path, long& fileSize) {
    std::ifstream file(path, std::ios::binary | std::ios::ate);
    if (!file) {
        throw std::runtime_error("Could not open file path " + path);
    }
    fileSize = file.tellg();
    file.seekg(0, std::ios::beg);

    auto buffer = std::make_unique<uint8_t[]>(fileSize);
    if (!file.read(reinterpret_cast<char *>(buffer.get()), fileSize)) {
        throw std::runtime_error("Error reading file " + path);
    }
    return buffer;
}

void printBytes(unsigned char* bytes, long& size) {
    for (long i = 0; i < size; i++) {
        printf("%d ", bytes[i]);
    }
    std::cout << std::endl;
}

void runVm() {
    // this is a testing path
    auto path = "/home/kumaraswamy/Documents/Eia64/VM/tests/hello.e";
    long fileSize;
    auto bytes = readBytes(path, fileSize);
    //printBytes(bytes.get(), fileSize);

    auto vm = new class vm(std::move(bytes), fileSize);
    vm->run();
    delete vm;
}

int main() {
    auto esmPath = "/home/kumaraswamy/Documents/Eia64/VM/tests/animation.esm";
    auto compiledPath = "/home/kumaraswamy/Documents/Eia64/VM/tests/hello.e";
    delete new assembler(esmPath, compiledPath);

    runVm();
    return 0;
}
