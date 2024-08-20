#include <iostream>
#include <memory>
#include <fstream>
#include <chrono>

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

long long fibonacci(int n) {
    if (n <= 1) return n;
    return fibonacci(n - 1) + fibonacci(n - 2);
}

void doFibTest() {
    auto start = std::chrono::high_resolution_clock::now();
    long long result = fibonacci(30);
    auto stop = std::chrono::high_resolution_clock::now();
    auto duration = std::chrono::duration_cast<std::chrono::milliseconds>(stop - start);
    std::cout << "Fibonacci(30) = " << result << std::endl;
    std::cout << "Time taken: " << duration.count() << " milliseconds" << std::endl;
}

void runVm() {
    doFibTest();

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
    auto esmPath = "/home/kumaraswamy/Documents/Eia64/VM/tests/slim_fib.esm";
    auto compiledPath = "/home/kumaraswamy/Documents/Eia64/VM/tests/hello.e";
    delete new assembler(esmPath, compiledPath);

    runVm();
    return 0;
}
