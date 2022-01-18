#pragma once

#include <malloc.h>
#include <stdlib.h>
#include <string.h>

inline void sfree(void *p) {
    if (p) {
        free(p);
        p = NULL;
    }
}

inline void *smalloc(size_t size) {
    void *p;
    p = malloc(size);
    if (!p) abort();
    return p;
}

inline void *srealloc(void *p, size_t size) {
    void *q;
    if (p) {
        q = realloc(p, size);
    } else {
        q = malloc(size);
    }
    if (!q) abort();
    return q;
}
