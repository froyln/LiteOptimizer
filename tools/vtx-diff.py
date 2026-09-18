#!/usr/bin/env python3
"""Compare two vertex dumps (tools/bench.sh --profile: bench/vtx-off vs bench/vtx-on, copied to
run/bench/<world>-vtx-{off,on}/) section by section: vanilla's BLOCK layout, 28 bytes per vertex
(pos 3f, colour 4ub, uv 2f, light 2s). Reports sections with different vertex counts, then, for
equal counts, which fields differ and where."""
import os, struct, sys, collections
a, b = sys.argv[1], sys.argv[2]
fa = set(os.listdir(a)); fb = set(os.listdir(b))
print("sections only in", a, len(fa - fb), sorted(fa - fb)[:8])
print("sections only in", b, len(fb - fa), sorted(fb - fa)[:8])
V = struct.Struct('<3f4B2f2h')
counts = collections.Counter(); examples = {}
for f in sorted(fa & fb):
    da = open(os.path.join(a, f), 'rb').read(); db = open(os.path.join(b, f), 'rb').read()
    if len(da) != len(db):
        counts['count'] += 1; examples.setdefault('count', []).append((f, len(da)//28, len(db)//28)); continue
    if da == db:
        counts['same'] += 1; continue
    counts['differ'] += 1
    fields = collections.Counter(); first = None
    n = len(da) // 28
    for i in range(n):
        va = V.unpack_from(da, i*28); vb = V.unpack_from(db, i*28)
        if va != vb:
            if va[:3] != vb[:3]: fields['pos'] += 1
            if va[3:7] != vb[3:7]: fields['color'] += 1
            if va[7:9] != vb[7:9]: fields['uv'] += 1
            if va[9:] != vb[9:]: fields['light'] += 1
            if first is None: first = (i, va, vb)
    # same multiset of vertices but different order?
    sa = sorted(da[i*28:(i+1)*28] for i in range(n)); sb = sorted(db[i*28:(i+1)*28] for i in range(n))
    order = 'order-only' if sa == sb else 'content'
    examples.setdefault('differ', []).append((f, n, dict(fields), order, first))
print(dict(counts))
for k, v in examples.items():
    for e in v[:12]: print(k, e)
