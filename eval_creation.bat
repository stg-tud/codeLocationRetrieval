@echo off
for %%r in (grblSvd.txt, grblSvd.txt) do (
	for /L %%i in (1, 1, 2) do gradlew run -Pmyargs="@eval_params/%%r" --console=plain >> eval_results/console_%%r
)
pause