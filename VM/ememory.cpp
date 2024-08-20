//
// Created by kumaraswamy on 8/16/24.
//

#include "ememory.h"

memory_frame *ememory::enterFrame() {
    auto frame = new memory_frame();
    frames.push(frame);
    return frame;
}

memory_frame *ememory::exitFrame() {
    delete frames.top();
    frames.pop();
    return frames.top();
}

void ememory::close() {
    delete frames.top();
    frames.pop();
}
