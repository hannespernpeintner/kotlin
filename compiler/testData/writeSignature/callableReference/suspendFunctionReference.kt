abstract class AbstractTest {
    abstract fun x(): suspend (String) -> Unit
}

class Test : AbstractTest() {
    override fun x() = ::suspendX

    private suspend fun suspendX(s: String) {}
}

// method: Test::x
// jvm signature:     ()Lkotlin/reflect/KFunction;
// generic signature: ()Lkotlin/reflect/KFunction<Lkotlin/Unit;>;
