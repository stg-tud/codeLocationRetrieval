package retrieval.lsi

import org.apache.commons.math3.linear.MatrixUtils
import org.apache.commons.math3.linear.RealMatrix
import org.apache.commons.math3.linear.SingularValueDecomposition
import termdocmatrix.TermDocumentMatrix
import java.io.*

class Svd(private val tdm: TermDocumentMatrix, private val storageLocation: String = "") {
    val u: RealMatrix
    val s: RealMatrix
    val vt: RealMatrix
    val v: RealMatrix

    val singularValues: DoubleArray
    val rank: Int
    val norm: Double
    val conditionNumber: Double
    val inverseConditionNumber: Double

    init {

        @Suppress("UNCHECKED_CAST")
        if(isDataStored()) {
            println("=== LOADING STORED SVD ===")
//            val (uData, sData, vtData) = loadUSVT()
//            u = MatrixUtils.createRealMatrix(uData)
//            s = MatrixUtils.createRealMatrix(sData)
//            vt = MatrixUtils.createRealMatrix(vtData)
//            singularValues = DoubleArray(Integer.min(s.rowDimension, s.columnDimension)) { 5.0 }

            val ois = ObjectInputStream(FileInputStream("$storageLocation/svd.ser"))
            val fields = ois.readObject() as ArrayList<Any>

            with(fields) {
                u   = MatrixUtils.createRealMatrix(get(0) as Array<DoubleArray>)
                s   = MatrixUtils.createRealMatrix(get(1) as Array<DoubleArray>)
                vt  = MatrixUtils.createRealMatrix(get(2) as Array<DoubleArray>)
                v   = MatrixUtils.createRealMatrix(get(3) as Array<DoubleArray>)

                singularValues = get(4) as DoubleArray

                rank                    = get(5) as Int
                norm                    = get(6) as Double
                conditionNumber         = get(7) as Double
                inverseConditionNumber  = get(8) as Double
            }

//            u = MatrixUtils.createRealMatrix(ois.readObject() as Array<DoubleArray>)
//            s = MatrixUtils.createRealMatrix(ois.readObject() as Array<DoubleArray>)
//            vt = MatrixUtils.createRealMatrix(ois.readObject() as Array<DoubleArray>)
//            v = MatrixUtils.createRealMatrix(ois.readObject() as Array<DoubleArray>)
//            singularValues = ois.readObject() as DoubleArray
//            rank = ois.readInt()
//            norm = ois.readDouble()
//            conditionNumber = ois.readDouble()
//            inverseConditionNumber = ois.readDouble()
        }
        else {
            println("=== COMPUTING NEW SVD ===")
            val svd = SingularValueDecomposition(MatrixUtils.createRealMatrix(tdm.data))

            u = svd.u
            s = svd.s
            vt = svd.vt
            v = svd.v
            singularValues = svd.singularValues
            rank = svd.rank
            norm = svd.norm
            conditionNumber = svd.conditionNumber
            inverseConditionNumber = svd.inverseConditionNumber

            // store the computed decomposition
//            storeUSVT()
            storeValues()
        }
    }

    private fun isDataStored(): Boolean {
        val rootDir = File(storageLocation)
        if(rootDir.listFiles().isEmpty()) {
            return false
        }

        val filesInDir = rootDir.listFiles()
        return filesInDir.contains(File("$storageLocation/svd.ser"))
    }

    private fun storeValues() {
        val oos = ObjectOutputStream(FileOutputStream("$storageLocation/svd.ser"))

        // Order: U, S, VT, V, singularValue, rank, norm, conditionNumber, inverseConditionNumber
        // Missing: solver, getCovariance()
        val fieldsList = ArrayList<Any>()
        fieldsList.apply {
            add(u.data)
            add(s.data)
            add(vt.data)
            add(v.data)
            add(singularValues)
            add(rank)
            add(norm)
            add(conditionNumber)
            add(inverseConditionNumber)
        }
        oos.writeObject(fieldsList)
    }
}