package space.themelon.eia64

import space.themelon.eia64.signatures.*
import space.themelon.eia64.signatures.Matching.matches

object CastTest {
    @JvmStatic
    fun main(args: Array<String>) {
        cast(Sign.ARRAY, ArrayExtension(Sign.INT))   // Array -> Array<N>
        cast(ArrayExtension(Sign.INT), Sign.ARRAY)  // Array<N> -> Array
        cast(ObjectExtension("meow"), Sign.OBJECT) // Object Extension -> Object
        cast(Sign.OBJECT, ObjectExtension("meow")) // Object -> Object Extension
    }

    private fun cast(
        receivedSignature: Signature,
        resultSignature: Signature,
    ) {
        if (resultSignature is SimpleSignature) {
            // Array == Array<N>
            if (resultSignature == Sign.ARRAY && receivedSignature is ArrayExtension) return
            // Simple == Simple
            if (resultSignature == receivedSignature) return
        }
        // Object to Object<N>
        if (resultSignature is ObjectExtension) {
            // from Object to Object extension
            if (receivedSignature == Sign.OBJECT) return
            if (receivedSignature !is ObjectExtension) {
                throw RuntimeException("Cannot cast object type to $resultSignature")
            }
            val expectClass = resultSignature.extensionClass
            val gotClass = receivedSignature.extensionClass
            if (gotClass != Sign.OBJECT_SIGN && expectClass != gotClass) {
                throw RuntimeException("Cannot cast class $gotClass into $expectClass")
            }
            return
        } else if (resultSignature is ArrayExtension) {
            // Cast attempt from Array (raw) to Array<N>
            if (receivedSignature == Sign.ARRAY) return
            if (receivedSignature !is ArrayExtension) {
                throw RuntimeException("Cannot cas into array type $receivedSignature")
            }
            val castArrayType = resultSignature.elementSignature
            val currentArrayType = receivedSignature.elementSignature
            if (!matches(currentArrayType,  castArrayType)) {
                throw RuntimeException("Cannot cast array element type $currentArrayType into $castArrayType")
            }
            return
        }

        if (resultSignature == Sign.ARRAY) {
            // Cast from Array<N> to Array
            if (receivedSignature is ArrayExtension || receivedSignature == Sign.ARRAY) return
        } else if (resultSignature == Sign.OBJECT) {
            if (receivedSignature is ObjectExtension || receivedSignature == Sign.OBJECT) return
        }

        throw RuntimeException("Cannot cast to $resultSignature")
    }
}