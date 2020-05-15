package retrieval.lsi

import main.Options
import org.apache.commons.math3.linear.MatrixUtils
import org.apache.commons.math3.linear.RealMatrix
import org.apache.commons.math3.linear.SingularValueDecomposition
import termdocmatrix.TermDocumentMatrix
import java.io.*

// TODO: later use info from term-document matrix to verify that loaded SVD matches with current corpus
class Svd(private val tdm: TermDocumentMatrix, private val storedSvdFile: File) {
    val u: RealMatrix
    val s: RealMatrix
    val v: RealMatrix
    val ut: RealMatrix
    val vt: RealMatrix

    val singularValues: DoubleArray
    val rank: Int
    val norm: Double
    val conditionNumber: Double
    val inverseConditionNumber: Double

    init {
        @Suppress("UNCHECKED_CAST")
        if(Options.isSvdOnly) {
            val svd = SingularValueDecomposition(MatrixUtils.createRealMatrix(tdm.data))
            u = svd.u
            s = svd.s
            v = svd.v
            ut = svd.ut
            vt = svd.vt
            singularValues = svd.singularValues
            rank = svd.rank
            norm = svd.norm
            conditionNumber = svd.conditionNumber
            inverseConditionNumber = svd.inverseConditionNumber
        }
        else if(isDataStored()) {
            println("=== LOADING STORED SVD ===")
            println("Reading SVD from: ${storedSvdFile.path}")
            val ois = ObjectInputStream(FileInputStream(storedSvdFile.path))

            val fieldsMap = ois.readObject() as Map<String, Any>
            with(fieldsMap) {
                singularValues = get("singularValues") as DoubleArray
                u   = MatrixUtils.createRealMatrix(get("u.data") as Array<DoubleArray>)
                v   = MatrixUtils.createRealMatrix(get("v.data") as Array<DoubleArray>)
                rank                    = get("rank") as Int
                norm                    = get("norm") as Double
                conditionNumber         = get("conditionNumber") as Double
                inverseConditionNumber  = get("inverseConditionNumber") as Double

                s   = MatrixUtils.createRealDiagonalMatrix(singularValues)
                ut  = u.transpose()
                vt  = v.transpose()
            }
        }
        else {
            println("=== COMPUTING NEW SVD ===")
            println("Writing SVD to: ${storedSvdFile.path}")
            
            val svd = SingularValueDecomposition(MatrixUtils.createRealMatrix(tdm.data))

            u = svd.u
            s = svd.s
            v = svd.v
            ut = svd.ut
            vt = svd.vt
            singularValues = svd.singularValues
            rank = svd.rank
            norm = svd.norm
            conditionNumber = svd.conditionNumber
            inverseConditionNumber = svd.inverseConditionNumber

            // store the computed decomposition
            storeValues()
        }
    }

    private fun isDataStored() = Options.outputDecompositionsDir.listFiles()?.contains(storedSvdFile) ?: false

    private fun storeValues() {
        val oos = ObjectOutputStream(FileOutputStream(storedSvdFile.path))

        // Use a map instead of a list so that we're not depending on the order in which we add elements
        // Missing: solver, getCovariance()
        val fieldsMap = mutableMapOf<String, Any>()
        fieldsMap.apply {
            put("singularValues", singularValues)
            put("u.data", u.data)
            put("v.data", v.data)
            put("rank", rank)
            put("norm", norm)
            put("conditionNumber", conditionNumber)
            put("inverseConditionNumber", inverseConditionNumber)

            // we don't have to store the following
//            put("s.data", s.data)     // can be computed via MatrixUtils.createRealDiagonalMatrix(singularValues)
//            put("ut.data", ut.data)   // can be computed via u.transpose()
//            put("vt.data", vt.data)   // can be computed via v.transpose()
        }
        oos.writeObject(fieldsMap)
    }
}