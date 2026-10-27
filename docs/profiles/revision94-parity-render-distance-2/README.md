# Revision 94 cumulative parity at fixed render distance

The exact revision-94 artifact (SHA-256 `faa2ba14b109f35c698c86d078c68dcd36e271ae4729aac0dca894c371986b68`) was tested on the stable `test` save for 6,000 integrated-server ticks in the disposable full-stack clone. Before the comparison, a two-run OFF/OFF repeatability control passed. The direct OFF/ON comparison also passed: all three state dumps are byte-identical with total hash `9494ea25e3dee2acba33a46502beb2369ea15a1ccc1797f0ba7130c729ee6341`.

The clone's client render distance was temporarily fixed at 2 for these runs to avoid the variable chunk-loading churn seen at render distance 10. The original clone setting (10) was restored afterward. The source save was read-only; each run used a fresh `_parity` copy, which the harness removed. This establishes cumulative server-state parity for the current candidate under the deterministic fixture settings. It does not establish performance gains or close the installed-mod feature checklist.

Evidence: [OFF](off.txt), [ON](on.txt), [OFF control](off-control.txt), and [ON client log](on-client.log).
