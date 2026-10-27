# Revision 100 verbose parity reference

The exact revision-100 artifact (SHA-256 `af32613c211a13d7e89216809a6b2e0bfb360a193b7f967c4b777c2f9b5ee86f`)
ran the stable `test` save for 6,000 integrated-server ticks at render distance 2, with all
optimizations disabled. Its state dump matches the known revision-94 OFF baseline exactly at total
hash `9494ea25e3dee2acba33a46502beb2369ea15a1ccc1797f0ba7130c729ee6341`. The dump and 1.6 MB
verbose detail file are included here.

This is one successful reference run, not a determinism pass. The follow-up OFF repeat was not
acknowledged by Prism within the harness startup timeout, so no second verbose dump exists. The
earlier non-verbose OFF/OFF attempt remains a failure because its first run differed only in chunk
`(-120, 44)`. The clone's original render distance was restored to 10 after testing.
