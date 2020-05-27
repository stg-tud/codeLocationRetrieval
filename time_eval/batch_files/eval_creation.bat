@echo off
for %%r in (grblSvd.txt, mosaicSvd.txt, svtSvd.txt, obsSvd.txt) do (
	for /L %%i in (1, 1, 10) do gradlew run -Pmyargs="@eval_params/%%r" --console=plain >> eval_results/console_%%r
)
pause