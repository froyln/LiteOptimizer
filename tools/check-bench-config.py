#!/usr/bin/env python3
"""Exercise bench.sh's actual config writer without launching or modifying an instance."""
from pathlib import Path
import json
import re
import subprocess
import tempfile

root = Path(__file__).resolve().parent.parent
writer = re.search(r"<<'PY'\n(.*?)\nPY", (root / 'tools/bench.sh').read_text(), re.S).group(1)
options = root / 'src/main/java/dev/froyln/liteoptimizer/config/Option.java'
with tempfile.TemporaryDirectory(prefix='bench-config-check-') as d:
    config = Path(d) / 'config.json'
    def run(mode, only='', compare=''):
        config.write_text('{"render":false,"_help":{}}')
        subprocess.run(['python3', '-', mode, only, str(config), str(options), compare], input=writer, text=True, check=True)
        return json.loads(config.read_text())
    off = run('off', compare='model_display_lists')
    on = run('on', compare='model_display_lists')
    assert {k for k in off if off[k] != on[k]} == {'model_display_lists'}
    assert not off['model_display_lists'] and on['model_display_lists']
    assert off['render'] and off['sodium'] and off['property_hashcode_cache']
    config.write_text('{"stale_key":true}')
    rejected = subprocess.run(['python3', '-', 'off', '', str(config), str(options), 'stale_key'], input=writer, text=True, capture_output=True)
    assert rejected.returncode != 0 and 'Unknown comparison key' in rejected.stderr
    assert not any(run('off').values())
    assert all(run('on').values())
    only = run('on', only='model_display_lists')
    assert only['render'] and only['model_display_lists'] and not only['property_hashcode_cache']
    ini_writer = re.findall(r"<<'PY'\n(.*?)\nPY", (root / 'tools/bench.sh').read_text(), re.S)[1]
    ini = Path(d) / 'instance.cfg'
    ini.write_text('[General]\nJvmArgs=old\nOverrideJavaArgs=false\n')
    args = '-Dliteoptimizer.bench.skiptesr=TileEntitySkull,TileEntityEnchantmentTable'
    subprocess.run(['python3', '-', str(ini), args, '0'], input=ini_writer, text=True, check=True)
    value = next(line.split('=', 1)[1] for line in ini.read_text().splitlines() if line.startswith('JvmArgs='))
    assert value.startswith('"') and json.loads(value) == args
    assert sum(line.startswith('JvmArgs=') for line in ini.read_text().splitlines()) == 1
    assert 'OverrideJavaArgs=true' in ini.read_text().splitlines()
print('Benchmark config check passed')
