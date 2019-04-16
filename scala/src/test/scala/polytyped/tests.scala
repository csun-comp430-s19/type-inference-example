package simplescala_experimentation.polytyped

object TestsHelper {
  import java.io.File
  def testPath(path: String): File = {
    //new File("../../../../test_programs/" + path)
    new File("test_programs/" + path)
  }
}
import TestsHelper.testPath

class BaseTests extends TestSuite(None, new DirectoryTestContainer(testPath("tests")))
class ListTests extends TestSuite(Some(testPath("lists.simplescala")), new DirectoryTestContainer(testPath("list_tests")))
class GeneratedWellTypedTests extends TestSuite(None, new ZipTestContainer(testPath("generated_well_typed.zip")))
class GeneratedIllTypedTests extends TestSuite(None, new ZipTestContainer(testPath("generated_ill_typed.zip")))
class GeneratedAlmostWellTypedTests extends TestSuite(None, new ZipTestContainer(testPath("generated_almost_well_typed.zip")))
