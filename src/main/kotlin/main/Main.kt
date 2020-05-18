package main

import main.console.ConsoleApplication
import main.console.LsiConsole
import main.console.VsmConsole
import org.apache.commons.math3.linear.RealMatrix

fun main(args: Array<String>) {
    println("ARGS = ${args.toList()}")
    Options.parse(args)

    val consoleApp = when(Options.irModel) {
        "lsi" -> LsiConsole()
        "vsm" -> VsmConsole()
        else -> object : ConsoleApplication() {
            override fun start() {
                println("Could not find an appropriate console for argument '${Options.irModel}'. Abort.")
                System.exit(64)
            }
        }
    }

    consoleApp.start()
}

// extension function for printing Commons Math RealMatrix
@Suppress("unused")
fun RealMatrix.display(matrixName: String = "A") {
    val data = this.data

    println("$matrixName = ")
    for(i in data.indices) {
        for(j in data[i].indices) {
            print("%8.4f ".format(data[i][j]))
        }
        println()
    }

    println()
}