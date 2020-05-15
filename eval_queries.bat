@echo off
for %%r in (grblQuery.txt, mosaicQuery.txt, svtQuery.txt, obsQuery.txt) do (
	for /L %%i in (1, 1, 100) do gradlew run -Pmyargs="@eval_params/%%r" --console=plain >> eval_results/console_%%r
)
pause