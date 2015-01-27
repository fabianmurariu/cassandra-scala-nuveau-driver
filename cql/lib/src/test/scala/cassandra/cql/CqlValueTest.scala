package cassandra.cql

import org.specs2.mutable.Specification

class CqlValueTest extends Specification {

  "CqlNull values" should {
    "be null" in {
      CqlNull.values === "null"
    }
  }

  "CqlNumber" should {
    "be 123" in {
      CqlNumber(123).values === "123"
    }
    "be 12.3" in {
      CqlNumber(12.3).values === "12.3"
    }
    "be 1234.45" in {
      CqlNumber(1234.45d).values === "1234.45"
    }
    "be really long" in {
      CqlNumber(BigInt("119347612398576203981203912038181476234")).values === "119347612398576203981203912038181476234"
    }
  }

  "CqlList" should {
    "be [1,2,3]" in {
      CqlList(CqlNumber(1),CqlNumber(2),CqlNumber(3)).values === "[1,2,3]"
    }
  }

  "CqlSet" should {
    "be {1,2,3,null}" in {
      CqlSet(CqlNumber(1),CqlNumber(2),CqlNumber(3), CqlNull).values === "{1,2,3,null}"
    }
  }

  "CqlBoolean" should {
    "be true" in {
      CqlTrue.values === "true"
    }
    "be false" in {
      CqlFalse.values === "false"
    }
  }

  "CqlText" should {
    "be 'something'" in {
      CqlText("something").values === "'something'"
    }
  }

  "CqlType" should {
    "be {'one':1,'two':2.0,'three':'three','not':false}" in {
      CqlType("whatever",
        "one" -> CqlNumber(1),
        "two" -> CqlNumber(2.0),
        "three" -> CqlText("three"),
        "not" -> CqlFalse
      ).values === "{'one':1,'two':2.0,'three':'three','not':false}"
    }
  }

  "CqlColumn" should {
    "be (1,'two',false,[1, 2],{'one':1,'two':2.0},{'a','b','c'})" in {
      CqlTable(
        CqlType("random",
          "1"->CqlNumber(1),
          "2" -> CqlText("two"),
          "3" -> CqlFalse,
          "4" -> CqlList(CqlNumber(1), CqlNumber(2)),
          "5" -> CqlType("five",
            "one" -> CqlNumber(1),
            "blah" -> CqlNumber(2.0)
          ),
          "6" -> CqlSet(CqlText("a"), CqlText("b"), CqlNull)
        )
      ).values === "(1,'two',false,[1,2],{'one':1,'blah':2.0},{'a','b',null})"
    }
  }

}
