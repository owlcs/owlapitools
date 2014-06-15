package owlapitools;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.HashSet;

import org.junit.Before;
import org.junit.Test;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.reasoner.Node;
import org.semanticweb.owlapi.reasoner.NodeSet;

import uk.ac.manchester.cs.jfact.JFactFactory;
import uk.ac.manchester.cs.jfact.JFactReasoner;

@SuppressWarnings("javadoc")
public class VerifyComplianceUniversityTestCase {

    private String input = "/AF_university.owl.xml";
    private JFactReasoner reasoner;
    private OWLDataFactory df = OWLManager.getOWLDataFactory();

    private OWLOntology load() throws OWLOntologyCreationException {
        return OWLManager.createOWLOntologyManager()
                .loadOntologyFromOntologyDocument(
                        VerifyComplianceUniversityTestCase.class
                                .getResourceAsStream(input));
    }

    @SuppressWarnings("rawtypes")
    private void equal(NodeSet<?> node, Object... objects) {
        assertEquals(new HashSet(Arrays.asList(objects)), node.getFlattened());
    }

    @SuppressWarnings("rawtypes")
    private void equal(Node<?> node, Object... objects) {
        assertEquals(new HashSet(Arrays.asList(objects)), node.getEntities());
    }

    @SuppressWarnings("rawtypes")
    private void equal(Object o, boolean object) {
        assertEquals(object, o);
    }

    private OWLClass C(String i) {
        return df.getOWLClass(IRI.create(i));
    }

    OWLClass assistantProfessor = C("http://www.mindswap.org/ontologies/debugging/university.owl#AssistantProfessor");
    OWLClass aiStudent = C("http://www.mindswap.org/ontologies/debugging/university.owl#AIStudent");
    OWLClass aiDept = C("http://www.mindswap.org/ontologies/debugging/university.owl#AI_Dept");
    OWLClass CS_Department = C("http://www.mindswap.org/ontologies/debugging/university.owl#CS_Department");
    OWLClass HCIStudent = C("http://www.mindswap.org/ontologies/debugging/university.owl#HCIStudent");
    OWLClass CS_StudentTakingCourses = C("http://www.mindswap.org/ontologies/debugging/university.owl#CS_StudentTakingCourses");
    OWLClass LecturerTaking4Courses = C("http://www.mindswap.org/ontologies/debugging/university.owl#LecturerTaking4Courses");
    OWLClass Lecturer = C("http://www.mindswap.org/ontologies/debugging/university.owl#Lecturer");
    OWLClass CS_Course = C("http://www.mindswap.org/ontologies/debugging/university.owl#CS_Course");
    OWLClass Professor = C("http://www.mindswap.org/ontologies/debugging/university.owl#Professor");
    OWLClass owlNothing = df.getOWLNothing();
    OWLClass owlThing = df.getOWLThing();
    OWLClass ProfessorInHCIorAI = C("http://www.mindswap.org/ontologies/debugging/university.owl#ProfessorInHCIorAI");
    OWLClass UniversityPhoneBook = C("http://www.mindswap.org/ontologies/debugging/university.owl#UniversityPhoneBook");
    OWLClass ResearchArea = C("http://www.mindswap.org/ontologies/debugging/university.owl#ResearchArea");
    OWLClass TeachingFaculty = C("http://www.mindswap.org/ontologies/debugging/university.owl#TeachingFaculty");
    OWLClass Department = C("http://www.mindswap.org/ontologies/debugging/university.owl#Department");
    OWLClass Person = C("http://www.mindswap.org/ontologies/debugging/university.owl#Person");
    OWLClass CS_Student = C("http://www.mindswap.org/ontologies/debugging/university.owl#CS_Student");
    OWLClass Student = C("http://www.mindswap.org/ontologies/debugging/university.owl#Student");
    OWLClass PhoneBook = C("http://www.mindswap.org/ontologies/debugging/university.owl#PhoneBook");
    OWLClass EE_Course = C("http://www.mindswap.org/ontologies/debugging/university.owl#EE_Course");
    OWLClass Faculty = C("http://www.mindswap.org/ontologies/debugging/university.owl#Faculty");
    OWLClass CS_Library = C("http://www.mindswap.org/ontologies/debugging/university.owl#CS_Library");
    OWLClass EE_Department = C("http://www.mindswap.org/ontologies/debugging/university.owl#EE_Department");
    OWLClass FacultyPhoneBook = C("http://www.mindswap.org/ontologies/debugging/university.owl#FacultyPhoneBook");
    OWLClass Schedule = C("http://www.mindswap.org/ontologies/debugging/university.owl#Schedule");
    OWLClass Library = C("http://www.mindswap.org/ontologies/debugging/university.owl#Library");
    OWLClass Course = C("http://www.mindswap.org/ontologies/debugging/university.owl#Course");
    OWLClass EE_Library = C("http://www.mindswap.org/ontologies/debugging/university.owl#EE_Library");
    OWLDataProperty hasTenure = df
            .getOWLDataProperty(IRI
                    .create("http://www.mindswap.org/ontologies/debugging/university.owl#hasTenure"));
    OWLDataProperty owlbottomDataProperty = df.getOWLBottomDataProperty();
    OWLDataProperty owltopDataProperty = df.getOWLTopDataProperty();
    OWLObjectProperty owltopObjectProperty = df.getOWLTopObjectProperty();
    OWLObjectProperty owlbottomObjectProperty = df.getOWLBottomObjectProperty();

    @Before
    public void setUp() throws OWLOntologyCreationException {
        reasoner = (JFactReasoner) new JFactFactory().createReasoner(load());
    }

    @Test
    public void shouldPassgetBottomClass() {
        equal(reasoner.getBottomClassNode(), assistantProfessor, aiStudent,
                aiDept, CS_Department, HCIStudent, CS_StudentTakingCourses,
                LecturerTaking4Courses, Lecturer, owlNothing, CS_Course);
    }

    @Test
    public void shouldPassgetUnsatisfiableClasses() {
        equal(reasoner.getUnsatisfiableClasses(), assistantProfessor,
                aiStudent, aiDept, CS_Department, HCIStudent,
                CS_StudentTakingCourses, LecturerTaking4Courses, Lecturer,
                owlNothing, CS_Course);
    }

    @Test
    public void shouldPassisSatisfiableProfessor() {
        equal(reasoner.isSatisfiable(Professor), true);
    }

    @Test
    public void shouldPassisSatisfiableProfessorInHCIorAI() {
        equal(reasoner.isSatisfiable(ProfessorInHCIorAI), true);
    }

    @Test
    public void shouldPassgetSubClassesowlThingtrue() {
        equal(reasoner.getSubClasses(owlThing, true), PhoneBook, ResearchArea,
                Department, Person, Library, Schedule, Course);
    }

    @Test
    public void shouldPassgetSubClassesowlThingfalse() {
        equal(reasoner.getSubClasses(owlThing, false), Student, PhoneBook,
                UniversityPhoneBook, CS_Student, ResearchArea, TeachingFaculty,
                Department, Person, Professor, EE_Course, Faculty, EE_Library,
                assistantProfessor, aiStudent, aiDept, CS_Department,
                HCIStudent, CS_StudentTakingCourses, LecturerTaking4Courses,
                Lecturer, owlNothing, CS_Course, CS_Library, EE_Department,
                ProfessorInHCIorAI, FacultyPhoneBook, Library, Schedule, Course);
    }

    @Test
    public void shouldPassgetDisjointClassesowlThing() {
        equal(reasoner.getDisjointClasses(owlThing), assistantProfessor,
                aiStudent, aiDept, CS_Department, HCIStudent,
                CS_StudentTakingCourses, LecturerTaking4Courses, Lecturer,
                owlNothing, CS_Course);
    }

    @Test
    public void shouldPassgetSubClassesUniversityPhoneBookfalse() {
        equal(reasoner.getSubClasses(UniversityPhoneBook, false),
                assistantProfessor, aiStudent, aiDept, CS_Department,
                HCIStudent, CS_StudentTakingCourses, LecturerTaking4Courses,
                Lecturer, owlNothing, CS_Course);
    }

    @Test
    public void shouldPassgetSubClassesUniversityPhoneBooktrue() {
        equal(reasoner.getSubClasses(UniversityPhoneBook, true),
                assistantProfessor, aiStudent, aiDept, CS_Department,
                HCIStudent, CS_StudentTakingCourses, LecturerTaking4Courses,
                Lecturer, owlNothing, CS_Course);
    }

    @Test
    public void shouldPassgetDisjointClassesUniversityPhoneBook() {
        equal(reasoner.getDisjointClasses(UniversityPhoneBook),
                assistantProfessor, aiStudent, aiDept, CS_Department,
                HCIStudent, CS_StudentTakingCourses, LecturerTaking4Courses,
                Lecturer, owlNothing, CS_Course);
    }

    @Test
    public void shouldPassgetSubClassesCS_Studentfalse() {
        equal(reasoner.getSubClasses(CS_Student, false), assistantProfessor,
                aiStudent, aiDept, CS_Department, HCIStudent,
                CS_StudentTakingCourses, LecturerTaking4Courses, Lecturer,
                owlNothing, CS_Course);
    }

    @Test
    public void shouldPassgetSubClassesCS_Studenttrue() {
        equal(reasoner.getSubClasses(CS_Student, true), assistantProfessor,
                aiStudent, aiDept, CS_Department, HCIStudent,
                CS_StudentTakingCourses, LecturerTaking4Courses, Lecturer,
                owlNothing, CS_Course);
    }

    @Test
    public void shouldPassgetDisjointClassesCS_Student() {
        equal(reasoner.getDisjointClasses(CS_Student), assistantProfessor,
                aiStudent, aiDept, CS_Department, HCIStudent,
                CS_StudentTakingCourses, LecturerTaking4Courses, Lecturer,
                owlNothing, CS_Course);
    }

    @Test
    public void shouldPassgetSubClassesResearchAreafalse() {
        equal(reasoner.getSubClasses(ResearchArea, false), assistantProfessor,
                aiStudent, aiDept, CS_Department, HCIStudent,
                CS_StudentTakingCourses, LecturerTaking4Courses, Lecturer,
                owlNothing, CS_Course);
    }

    @Test
    public void shouldPassgetSubClassesResearchAreatrue() {
        equal(reasoner.getSubClasses(ResearchArea, true), assistantProfessor,
                aiStudent, aiDept, CS_Department, HCIStudent,
                CS_StudentTakingCourses, LecturerTaking4Courses, Lecturer,
                owlNothing, CS_Course);
    }

    @Test
    public void shouldPassgetDisjointClassesResearchArea() {
        equal(reasoner.getDisjointClasses(ResearchArea), assistantProfessor,
                aiStudent, aiDept, CS_Department, HCIStudent,
                CS_StudentTakingCourses, LecturerTaking4Courses, Lecturer,
                owlNothing, CS_Course);
    }

    @Test
    public void shouldPassgetSubClassesDepartmentfalse() {
        equal(reasoner.getSubClasses(Department, false), CS_Library,
                assistantProfessor, aiStudent, aiDept, CS_Department,
                HCIStudent, CS_StudentTakingCourses, LecturerTaking4Courses,
                Lecturer, owlNothing, CS_Course, EE_Department);
    }

    @Test
    public void shouldPassgetDisjointClassesDepartment() {
        equal(reasoner.getDisjointClasses(Department), assistantProfessor,
                aiStudent, aiDept, CS_Department, HCIStudent,
                CS_StudentTakingCourses, LecturerTaking4Courses, Lecturer,
                owlNothing, CS_Course);
    }

    @Test
    public void shouldPassgetSubClassesTeachingFacultyfalse() {
        equal(reasoner.getSubClasses(TeachingFaculty, false),
                assistantProfessor, aiStudent, aiDept, CS_Department,
                HCIStudent, CS_StudentTakingCourses, LecturerTaking4Courses,
                Lecturer, owlNothing, CS_Course, ProfessorInHCIorAI, Professor);
    }

    @Test
    public void shouldPassgetSubClassesTeachingFacultytrue() {
        equal(reasoner.getSubClasses(TeachingFaculty, true), Professor);
    }

    @Test
    public void shouldPassgetDisjointClassesTeachingFaculty() {
        equal(reasoner.getDisjointClasses(TeachingFaculty), assistantProfessor,
                aiStudent, aiDept, CS_Department, HCIStudent,
                CS_StudentTakingCourses, LecturerTaking4Courses, Lecturer,
                owlNothing, CS_Course);
    }

    @Test
    public void shouldPassgetSuperClassesHCIStudentfalse() {
        equal(reasoner.getSuperClasses(HCIStudent, false), owlThing, Student,
                PhoneBook, UniversityPhoneBook, CS_Student, ResearchArea,
                TeachingFaculty, Department, Person, Professor, EE_Course,
                Faculty, EE_Library, CS_Library, EE_Department,
                ProfessorInHCIorAI, FacultyPhoneBook, Library, Schedule, Course);
    }

    @Test
    public void shouldPassgetSuperClassesHCIStudenttrue() {
        equal(reasoner.getSuperClasses(HCIStudent, true), EE_Library,
                CS_Student, CS_Library, UniversityPhoneBook, ResearchArea,
                ProfessorInHCIorAI, FacultyPhoneBook, EE_Course, Schedule);
    }

    @Test
    public void shouldPassgetEquivalentClassesHCIStudent() {
        equal(reasoner.getEquivalentClasses(HCIStudent), assistantProfessor,
                aiStudent, aiDept, CS_Department, HCIStudent,
                CS_StudentTakingCourses, LecturerTaking4Courses, Lecturer,
                owlNothing, CS_Course);
    }

    @Test
    public void shouldPassgetDisjointClassesHCIStudent() {
        equal(reasoner.getDisjointClasses(HCIStudent), owlThing, Student,
                PhoneBook, CS_Student, UniversityPhoneBook, ResearchArea,
                TeachingFaculty, Department, Person, Professor, EE_Course,
                Faculty, EE_Library, assistantProfessor, aiStudent, aiDept,
                CS_Department, HCIStudent, CS_StudentTakingCourses,
                LecturerTaking4Courses, Lecturer, owlNothing, CS_Course,
                CS_Library, EE_Department, ProfessorInHCIorAI,
                FacultyPhoneBook, Library, Schedule, Course);
    }

    @Test
    public void shouldPassgetSubClassesPersonfalse() {
        equal(reasoner.getSubClasses(Person, false), Student, Faculty,
                CS_Student, assistantProfessor, aiStudent, aiDept,
                CS_Department, HCIStudent, CS_StudentTakingCourses,
                LecturerTaking4Courses, Lecturer, owlNothing, CS_Course,
                TeachingFaculty, ProfessorInHCIorAI, Professor);
    }

    @Test
    public void shouldPassgetDisjointClassesPerson() {
        equal(reasoner.getDisjointClasses(Person), assistantProfessor,
                aiStudent, aiDept, CS_Department, HCIStudent,
                CS_StudentTakingCourses, LecturerTaking4Courses, Lecturer,
                owlNothing, CS_Course);
    }

    @Test
    public void shouldPassgetSubClassesProfessorfalse() {
        equal(reasoner.getSubClasses(Professor, false), assistantProfessor,
                aiStudent, aiDept, CS_Department, HCIStudent,
                CS_StudentTakingCourses, LecturerTaking4Courses, Lecturer,
                owlNothing, CS_Course, ProfessorInHCIorAI);
    }

    @Test
    public void shouldPassgetSuperClassesProfessorfalse() {
        equal(reasoner.getSuperClasses(Professor, false), owlThing, Faculty,
                TeachingFaculty, Person);
    }

    @Test
    public void shouldPassgetSubClassesProfessortrue() {
        equal(reasoner.getSubClasses(Professor, true), ProfessorInHCIorAI);
    }

    @Test
    public void shouldPassgetSuperClassesProfessortrue() {
        equal(reasoner.getSuperClasses(Professor, true), TeachingFaculty);
    }

    @Test
    public void shouldPassgetEquivalentClassesProfessor() {
        equal(reasoner.getEquivalentClasses(Professor), Professor);
    }

    @Test
    public void shouldPassgetDisjointClassesProfessor() {
        equal(reasoner.getDisjointClasses(Professor), assistantProfessor,
                aiStudent, aiDept, CS_Department, HCIStudent,
                CS_StudentTakingCourses, LecturerTaking4Courses, Lecturer,
                owlNothing, CS_Course);
    }

    @Test
    public void shouldPassgetSubClassesEE_Coursefalse() {
        equal(reasoner.getSubClasses(EE_Course, false), assistantProfessor,
                aiStudent, aiDept, CS_Department, HCIStudent,
                CS_StudentTakingCourses, LecturerTaking4Courses, Lecturer,
                owlNothing, CS_Course);
    }

    @Test
    public void shouldPassgetSubClassesEE_Coursetrue() {
        equal(reasoner.getSubClasses(EE_Course, true), assistantProfessor,
                aiStudent, aiDept, CS_Department, HCIStudent,
                CS_StudentTakingCourses, LecturerTaking4Courses, Lecturer,
                owlNothing, CS_Course);
    }

    @Test
    public void shouldPassgetDisjointClassesEE_Course() {
        equal(reasoner.getDisjointClasses(EE_Course), assistantProfessor,
                aiStudent, aiDept, CS_Department, HCIStudent,
                CS_StudentTakingCourses, LecturerTaking4Courses, Lecturer,
                owlNothing, CS_Course);
    }

    @Test
    public void shouldPassgetSubClassesFacultyfalse() {
        equal(reasoner.getSubClasses(Faculty, false), assistantProfessor,
                aiStudent, aiDept, CS_Department, HCIStudent,
                CS_StudentTakingCourses, LecturerTaking4Courses, Lecturer,
                owlNothing, CS_Course, TeachingFaculty, ProfessorInHCIorAI,
                Professor);
    }

    @Test
    public void shouldPassgetDisjointClassesFaculty() {
        equal(reasoner.getDisjointClasses(Faculty), assistantProfessor,
                aiStudent, aiDept, CS_Department, HCIStudent,
                CS_StudentTakingCourses, LecturerTaking4Courses, Lecturer,
                owlNothing, CS_Course);
    }

    @Test
    public void shouldPassgetSubClassesEE_Libraryfalse() {
        equal(reasoner.getSubClasses(EE_Library, false), assistantProfessor,
                aiStudent, aiDept, CS_Department, HCIStudent,
                CS_StudentTakingCourses, LecturerTaking4Courses, Lecturer,
                owlNothing, CS_Course);
    }

    @Test
    public void shouldPassgetSubClassesEE_Librarytrue() {
        equal(reasoner.getSubClasses(EE_Library, true), assistantProfessor,
                aiStudent, aiDept, CS_Department, HCIStudent,
                CS_StudentTakingCourses, LecturerTaking4Courses, Lecturer,
                owlNothing, CS_Course);
    }

    @Test
    public void shouldPassgetDisjointClassesEE_Library() {
        equal(reasoner.getDisjointClasses(EE_Library), assistantProfessor,
                aiStudent, aiDept, CS_Department, HCIStudent,
                CS_StudentTakingCourses, LecturerTaking4Courses, Lecturer,
                owlNothing, CS_Course);
    }

    @Test
    public void shouldPassgetSubClassesEE_Departmentfalse() {
        equal(reasoner.getSubClasses(EE_Department, false), CS_Library,
                assistantProfessor, aiStudent, aiDept, CS_Department,
                HCIStudent, CS_StudentTakingCourses, LecturerTaking4Courses,
                Lecturer, owlNothing, CS_Course);
    }

    @Test
    public void shouldPassgetDisjointClassesEE_Department() {
        equal(reasoner.getDisjointClasses(EE_Department), assistantProfessor,
                aiStudent, aiDept, CS_Department, HCIStudent,
                CS_StudentTakingCourses, LecturerTaking4Courses, Lecturer,
                owlNothing, CS_Course);
    }

    @Test
    public void shouldPassgetSubClassesFacultyPhoneBookfalse() {
        equal(reasoner.getSubClasses(FacultyPhoneBook, false),
                assistantProfessor, aiStudent, aiDept, CS_Department,
                HCIStudent, CS_StudentTakingCourses, LecturerTaking4Courses,
                Lecturer, owlNothing, CS_Course);
    }

    @Test
    public void shouldPassgetSubClassesFacultyPhoneBooktrue() {
        equal(reasoner.getSubClasses(FacultyPhoneBook, true),
                assistantProfessor, aiStudent, aiDept, CS_Department,
                HCIStudent, CS_StudentTakingCourses, LecturerTaking4Courses,
                Lecturer, owlNothing, CS_Course);
    }

    @Test
    public void shouldPassgetDisjointClassesFacultyPhoneBook() {
        equal(reasoner.getDisjointClasses(FacultyPhoneBook),
                assistantProfessor, aiStudent, aiDept, CS_Department,
                HCIStudent, CS_StudentTakingCourses, LecturerTaking4Courses,
                Lecturer, owlNothing, CS_Course);
    }

    @Test
    public void shouldPassgetSuperClassesLecturerfalse() {
        equal(reasoner.getSuperClasses(Lecturer, false), owlThing, Student,
                PhoneBook, UniversityPhoneBook, CS_Student, ResearchArea,
                TeachingFaculty, Department, Person, Professor, EE_Course,
                Faculty, EE_Library, CS_Library, EE_Department,
                ProfessorInHCIorAI, FacultyPhoneBook, Library, Schedule, Course);
    }

    @Test
    public void shouldPassgetSuperClassesLecturertrue() {
        equal(reasoner.getSuperClasses(Lecturer, true), EE_Library, CS_Student,
                CS_Library, UniversityPhoneBook, ResearchArea,
                ProfessorInHCIorAI, FacultyPhoneBook, EE_Course, Schedule);
    }

    @Test
    public void shouldPassgetEquivalentClassesLecturer() {
        equal(reasoner.getEquivalentClasses(Lecturer), assistantProfessor,
                aiStudent, aiDept, CS_Department, HCIStudent,
                CS_StudentTakingCourses, LecturerTaking4Courses, Lecturer,
                owlNothing, CS_Course);
    }

    @Test
    public void shouldPassgetDisjointClassesLecturer() {
        equal(reasoner.getDisjointClasses(Lecturer), owlThing, Student,
                PhoneBook, CS_Student, UniversityPhoneBook, ResearchArea,
                TeachingFaculty, Department, Person, Professor, EE_Course,
                Faculty, EE_Library, assistantProfessor, aiStudent, aiDept,
                CS_Department, HCIStudent, CS_StudentTakingCourses,
                LecturerTaking4Courses, Lecturer, owlNothing, CS_Course,
                CS_Library, EE_Department, ProfessorInHCIorAI,
                FacultyPhoneBook, Library, Schedule, Course);
    }

    @Test
    public void shouldPassgetSubClassesLibraryfalse() {
        equal(reasoner.getSubClasses(Library, false), EE_Library, CS_Library,
                assistantProfessor, aiStudent, aiDept, CS_Department,
                HCIStudent, CS_StudentTakingCourses, LecturerTaking4Courses,
                Lecturer, owlNothing, CS_Course);
    }

    @Test
    public void shouldPassgetDisjointClassesLibrary() {
        equal(reasoner.getDisjointClasses(Library), assistantProfessor,
                aiStudent, aiDept, CS_Department, HCIStudent,
                CS_StudentTakingCourses, LecturerTaking4Courses, Lecturer,
                owlNothing, CS_Course);
    }

    @Test
    public void shouldPassgetSuperClassesLecturerTaking4Coursesfalse() {
        equal(reasoner.getSuperClasses(LecturerTaking4Courses, false),
                owlThing, Student, PhoneBook, UniversityPhoneBook, CS_Student,
                ResearchArea, TeachingFaculty, Department, Person, Professor,
                EE_Course, Faculty, EE_Library, CS_Library, EE_Department,
                ProfessorInHCIorAI, FacultyPhoneBook, Library, Schedule, Course);
    }

    @Test
    public void shouldPassgetSuperClassesLecturerTaking4Coursestrue() {
        equal(reasoner.getSuperClasses(LecturerTaking4Courses, true),
                EE_Library, CS_Student, CS_Library, UniversityPhoneBook,
                ResearchArea, ProfessorInHCIorAI, FacultyPhoneBook, EE_Course,
                Schedule);
    }

    @Test
    public void shouldPassgetEquivalentClassesLecturerTaking4Courses() {
        equal(reasoner.getEquivalentClasses(LecturerTaking4Courses),
                assistantProfessor, aiStudent, aiDept, CS_Department,
                HCIStudent, CS_StudentTakingCourses, LecturerTaking4Courses,
                Lecturer, owlNothing, CS_Course);
    }

    @Test
    public void shouldPassgetDisjointClassesLecturerTaking4Courses() {
        equal(reasoner.getDisjointClasses(LecturerTaking4Courses), owlThing,
                Student, PhoneBook, CS_Student, UniversityPhoneBook,
                ResearchArea, TeachingFaculty, Department, Person, Professor,
                EE_Course, Faculty, EE_Library, assistantProfessor, aiStudent,
                aiDept, CS_Department, HCIStudent, CS_StudentTakingCourses,
                LecturerTaking4Courses, Lecturer, owlNothing, CS_Course,
                CS_Library, EE_Department, ProfessorInHCIorAI,
                FacultyPhoneBook, Library, Schedule, Course);
    }

    @Test
    public void shouldPassgetSubClassesSchedulefalse() {
        equal(reasoner.getSubClasses(Schedule, false), assistantProfessor,
                aiStudent, aiDept, CS_Department, HCIStudent,
                CS_StudentTakingCourses, LecturerTaking4Courses, Lecturer,
                owlNothing, CS_Course);
    }

    @Test
    public void shouldPassgetSubClassesScheduletrue() {
        equal(reasoner.getSubClasses(Schedule, true), assistantProfessor,
                aiStudent, aiDept, CS_Department, HCIStudent,
                CS_StudentTakingCourses, LecturerTaking4Courses, Lecturer,
                owlNothing, CS_Course);
    }

    @Test
    public void shouldPassgetDisjointClassesSchedule() {
        equal(reasoner.getDisjointClasses(Schedule), assistantProfessor,
                aiStudent, aiDept, CS_Department, HCIStudent,
                CS_StudentTakingCourses, LecturerTaking4Courses, Lecturer,
                owlNothing, CS_Course);
    }

    @Test
    public void shouldPassgetSuperClassesassistantProfessorfalse() {
        equal(reasoner.getSuperClasses(assistantProfessor, false), owlThing,
                Student, PhoneBook, UniversityPhoneBook, CS_Student,
                ResearchArea, TeachingFaculty, Department, Person, Professor,
                EE_Course, Faculty, EE_Library, CS_Library, EE_Department,
                ProfessorInHCIorAI, FacultyPhoneBook, Library, Schedule, Course);
    }

    @Test
    public void shouldPassgetSuperClassesassistantProfessortrue() {
        equal(reasoner.getSuperClasses(assistantProfessor, true), EE_Library,
                CS_Student, CS_Library, UniversityPhoneBook, ResearchArea,
                ProfessorInHCIorAI, FacultyPhoneBook, EE_Course, Schedule);
    }

    @Test
    public void shouldPassgetEquivalentClassesassistantProfessor() {
        equal(reasoner.getEquivalentClasses(assistantProfessor),
                assistantProfessor, aiStudent, aiDept, CS_Department,
                HCIStudent, CS_StudentTakingCourses, LecturerTaking4Courses,
                Lecturer, owlNothing, CS_Course);
    }

    @Test
    public void shouldPassgetDisjointClassesassistantProfessor() {
        equal(reasoner.getDisjointClasses(assistantProfessor), owlThing,
                Student, PhoneBook, CS_Student, UniversityPhoneBook,
                ResearchArea, TeachingFaculty, Department, Person, Professor,
                EE_Course, Faculty, EE_Library, assistantProfessor, aiStudent,
                aiDept, CS_Department, HCIStudent, CS_StudentTakingCourses,
                LecturerTaking4Courses, Lecturer, owlNothing, CS_Course,
                CS_Library, EE_Department, ProfessorInHCIorAI,
                FacultyPhoneBook, Library, Schedule, Course);
    }

    @Test
    public void shouldPassgetSubClassesStudentfalse() {
        equal(reasoner.getSubClasses(Student, false), CS_Student,
                assistantProfessor, aiStudent, aiDept, CS_Department,
                HCIStudent, CS_StudentTakingCourses, LecturerTaking4Courses,
                Lecturer, owlNothing, CS_Course);
    }

    @Test
    public void shouldPassgetDisjointClassesStudent() {
        equal(reasoner.getDisjointClasses(Student), assistantProfessor,
                aiStudent, aiDept, CS_Department, HCIStudent,
                CS_StudentTakingCourses, LecturerTaking4Courses, Lecturer,
                owlNothing, CS_Course);
    }

    @Test
    public void shouldPassgetSubClassesPhoneBookfalse() {
        equal(reasoner.getSubClasses(PhoneBook, false), UniversityPhoneBook,
                assistantProfessor, aiStudent, aiDept, CS_Department,
                HCIStudent, CS_StudentTakingCourses, LecturerTaking4Courses,
                Lecturer, owlNothing, CS_Course, FacultyPhoneBook);
    }

    @Test
    public void shouldPassgetDisjointClassesPhoneBook() {
        equal(reasoner.getDisjointClasses(PhoneBook), assistantProfessor,
                aiStudent, aiDept, CS_Department, HCIStudent,
                CS_StudentTakingCourses, LecturerTaking4Courses, Lecturer,
                owlNothing, CS_Course);
    }

    @Test
    public void shouldPassgetSuperClassesaiDeptfalse() {
        equal(reasoner.getSuperClasses(aiDept, false), owlThing, Student,
                PhoneBook, UniversityPhoneBook, CS_Student, ResearchArea,
                TeachingFaculty, Department, Person, Professor, EE_Course,
                Faculty, EE_Library, CS_Library, EE_Department,
                ProfessorInHCIorAI, FacultyPhoneBook, Library, Schedule, Course);
    }

    @Test
    public void shouldPassgetSuperClassesaiDepttrue() {
        equal(reasoner.getSuperClasses(aiDept, true), EE_Library, CS_Student,
                CS_Library, UniversityPhoneBook, ResearchArea,
                ProfessorInHCIorAI, FacultyPhoneBook, EE_Course, Schedule);
    }

    @Test
    public void shouldPassgetEquivalentClassesaiDept() {
        equal(reasoner.getEquivalentClasses(aiDept), assistantProfessor,
                aiStudent, aiDept, CS_Department, HCIStudent,
                CS_StudentTakingCourses, LecturerTaking4Courses, Lecturer,
                owlNothing, CS_Course);
    }

    @Test
    public void shouldPassgetDisjointClassesaiDept() {
        equal(reasoner.getDisjointClasses(aiDept), owlThing, Student,
                PhoneBook, CS_Student, UniversityPhoneBook, ResearchArea,
                TeachingFaculty, Department, Person, Professor, EE_Course,
                Faculty, EE_Library, assistantProfessor, aiStudent, aiDept,
                CS_Department, HCIStudent, CS_StudentTakingCourses,
                LecturerTaking4Courses, Lecturer, owlNothing, CS_Course,
                CS_Library, EE_Department, ProfessorInHCIorAI,
                FacultyPhoneBook, Library, Schedule, Course);
    }

    @Test
    public void shouldPassgetSuperClassesCS_Departmentfalse() {
        equal(reasoner.getSuperClasses(CS_Department, false), owlThing,
                Student, PhoneBook, UniversityPhoneBook, CS_Student,
                ResearchArea, TeachingFaculty, Department, Person, Professor,
                EE_Course, Faculty, EE_Library, CS_Library, EE_Department,
                ProfessorInHCIorAI, FacultyPhoneBook, Library, Schedule, Course);
    }

    @Test
    public void shouldPassgetSuperClassesCS_Departmenttrue() {
        equal(reasoner.getSuperClasses(CS_Department, true), EE_Library,
                CS_Student, CS_Library, UniversityPhoneBook, ResearchArea,
                ProfessorInHCIorAI, FacultyPhoneBook, EE_Course, Schedule);
    }

    @Test
    public void shouldPassgetEquivalentClassesCS_Department() {
        equal(reasoner.getEquivalentClasses(CS_Department), assistantProfessor,
                aiStudent, aiDept, CS_Department, HCIStudent,
                CS_StudentTakingCourses, LecturerTaking4Courses, Lecturer,
                owlNothing, CS_Course);
    }

    @Test
    public void shouldPassgetDisjointClassesCS_Department() {
        equal(reasoner.getDisjointClasses(CS_Department), owlThing, Student,
                PhoneBook, CS_Student, UniversityPhoneBook, ResearchArea,
                TeachingFaculty, Department, Person, Professor, EE_Course,
                Faculty, EE_Library, assistantProfessor, aiStudent, aiDept,
                CS_Department, HCIStudent, CS_StudentTakingCourses,
                LecturerTaking4Courses, Lecturer, owlNothing, CS_Course,
                CS_Library, EE_Department, ProfessorInHCIorAI,
                FacultyPhoneBook, Library, Schedule, Course);
    }

    @Test
    public void shouldPassgetSuperClassesaiStudentfalse() {
        equal(reasoner.getSuperClasses(aiStudent, false), owlThing, Student,
                PhoneBook, UniversityPhoneBook, CS_Student, ResearchArea,
                TeachingFaculty, Department, Person, Professor, EE_Course,
                Faculty, EE_Library, CS_Library, EE_Department,
                ProfessorInHCIorAI, FacultyPhoneBook, Library, Schedule, Course);
    }

    @Test
    public void shouldPassgetSuperClassesaiStudenttrue() {
        equal(reasoner.getSuperClasses(aiStudent, true), EE_Library,
                CS_Student, CS_Library, UniversityPhoneBook, ResearchArea,
                ProfessorInHCIorAI, FacultyPhoneBook, EE_Course, Schedule);
    }

    @Test
    public void shouldPassgetEquivalentClassesaiStudent() {
        equal(reasoner.getEquivalentClasses(aiStudent), assistantProfessor,
                aiStudent, aiDept, CS_Department, HCIStudent,
                CS_StudentTakingCourses, LecturerTaking4Courses, Lecturer,
                owlNothing, CS_Course);
    }

    @Test
    public void shouldPassgetDisjointClassesaiStudent() {
        equal(reasoner.getDisjointClasses(aiStudent), owlThing, Student,
                PhoneBook, CS_Student, UniversityPhoneBook, ResearchArea,
                TeachingFaculty, Department, Person, Professor, EE_Course,
                Faculty, EE_Library, assistantProfessor, aiStudent, aiDept,
                CS_Department, HCIStudent, CS_StudentTakingCourses,
                LecturerTaking4Courses, Lecturer, owlNothing, CS_Course,
                CS_Library, EE_Department, ProfessorInHCIorAI,
                FacultyPhoneBook, Library, Schedule, Course);
    }

    @Test
    public void shouldPassgetSubClassesCS_Libraryfalse() {
        equal(reasoner.getSubClasses(CS_Library, false), assistantProfessor,
                aiStudent, aiDept, CS_Department, HCIStudent,
                CS_StudentTakingCourses, LecturerTaking4Courses, Lecturer,
                owlNothing, CS_Course);
    }

    @Test
    public void shouldPassgetSubClassesCS_Librarytrue() {
        equal(reasoner.getSubClasses(CS_Library, true), assistantProfessor,
                aiStudent, aiDept, CS_Department, HCIStudent,
                CS_StudentTakingCourses, LecturerTaking4Courses, Lecturer,
                owlNothing, CS_Course);
    }

    @Test
    public void shouldPassgetDisjointClassesCS_Library() {
        equal(reasoner.getDisjointClasses(CS_Library), assistantProfessor,
                aiStudent, aiDept, CS_Department, HCIStudent,
                CS_StudentTakingCourses, LecturerTaking4Courses, Lecturer,
                owlNothing, CS_Course);
    }

    @Test
    public void shouldPassgetSubClassesProfessorInHCIorAIfalse() {
        equal(reasoner.getSubClasses(ProfessorInHCIorAI, false),
                assistantProfessor, aiStudent, aiDept, CS_Department,
                HCIStudent, CS_StudentTakingCourses, LecturerTaking4Courses,
                Lecturer, owlNothing, CS_Course);
    }

    @Test
    public void shouldPassgetSuperClassesProfessorInHCIorAIfalse() {
        equal(reasoner.getSuperClasses(ProfessorInHCIorAI, false), owlThing,
                Faculty, TeachingFaculty, Person, Professor);
    }

    @Test
    public void shouldPassgetSubClassesProfessorInHCIorAItrue() {
        equal(reasoner.getSubClasses(ProfessorInHCIorAI, true),
                assistantProfessor, aiStudent, aiDept, CS_Department,
                HCIStudent, CS_StudentTakingCourses, LecturerTaking4Courses,
                Lecturer, owlNothing, CS_Course);
    }

    @Test
    public void shouldPassgetSuperClassesProfessorInHCIorAItrue() {
        equal(reasoner.getSuperClasses(ProfessorInHCIorAI, true), Professor);
    }

    @Test
    public void shouldPassgetEquivalentClassesProfessorInHCIorAI() {
        equal(reasoner.getEquivalentClasses(ProfessorInHCIorAI),
                ProfessorInHCIorAI);
    }

    @Test
    public void shouldPassgetDisjointClassesProfessorInHCIorAI() {
        equal(reasoner.getDisjointClasses(ProfessorInHCIorAI),
                assistantProfessor, aiStudent, aiDept, CS_Department,
                HCIStudent, CS_StudentTakingCourses, LecturerTaking4Courses,
                Lecturer, owlNothing, CS_Course);
    }

    @Test
    public void shouldPassgetSuperClassesCS_StudentTakingCoursesfalse() {
        equal(reasoner.getSuperClasses(CS_StudentTakingCourses, false),
                owlThing, Student, PhoneBook, UniversityPhoneBook, CS_Student,
                ResearchArea, TeachingFaculty, Department, Person, Professor,
                EE_Course, Faculty, EE_Library, CS_Library, EE_Department,
                ProfessorInHCIorAI, FacultyPhoneBook, Library, Schedule, Course);
    }

    @Test
    public void shouldPassgetSuperClassesCS_StudentTakingCoursestrue() {
        equal(reasoner.getSuperClasses(CS_StudentTakingCourses, true),
                EE_Library, CS_Student, CS_Library, UniversityPhoneBook,
                ResearchArea, ProfessorInHCIorAI, FacultyPhoneBook, EE_Course,
                Schedule);
    }

    @Test
    public void shouldPassgetEquivalentClassesCS_StudentTakingCourses() {
        equal(reasoner.getEquivalentClasses(CS_StudentTakingCourses),
                assistantProfessor, aiStudent, aiDept, CS_Department,
                HCIStudent, CS_StudentTakingCourses, LecturerTaking4Courses,
                Lecturer, owlNothing, CS_Course);
    }

    @Test
    public void shouldPassgetDisjointClassesCS_StudentTakingCourses() {
        equal(reasoner.getDisjointClasses(CS_StudentTakingCourses), owlThing,
                Student, PhoneBook, CS_Student, UniversityPhoneBook,
                ResearchArea, TeachingFaculty, Department, Person, Professor,
                EE_Course, Faculty, EE_Library, assistantProfessor, aiStudent,
                aiDept, CS_Department, HCIStudent, CS_StudentTakingCourses,
                LecturerTaking4Courses, Lecturer, owlNothing, CS_Course,
                CS_Library, EE_Department, ProfessorInHCIorAI,
                FacultyPhoneBook, Library, Schedule, Course);
    }

    @Test
    public void shouldPassgetSubClassesCoursefalse() {
        equal(reasoner.getSubClasses(Course, false), assistantProfessor,
                aiStudent, aiDept, CS_Department, HCIStudent,
                CS_StudentTakingCourses, LecturerTaking4Courses, Lecturer,
                owlNothing, CS_Course, EE_Course);
    }

    @Test
    public void shouldPassgetDisjointClassesCourse() {
        equal(reasoner.getDisjointClasses(Course), assistantProfessor,
                aiStudent, aiDept, CS_Department, HCIStudent,
                CS_StudentTakingCourses, LecturerTaking4Courses, Lecturer,
                owlNothing, CS_Course);
    }

    @Test
    public void shouldPassgetSuperClassesCS_Coursefalse() {
        equal(reasoner.getSuperClasses(CS_Course, false), owlThing, Student,
                PhoneBook, UniversityPhoneBook, CS_Student, ResearchArea,
                TeachingFaculty, Department, Person, Professor, EE_Course,
                Faculty, EE_Library, CS_Library, EE_Department,
                ProfessorInHCIorAI, FacultyPhoneBook, Library, Schedule, Course);
    }

    @Test
    public void shouldPassgetSuperClassesCS_Coursetrue() {
        equal(reasoner.getSuperClasses(CS_Course, true), EE_Library,
                CS_Student, CS_Library, UniversityPhoneBook, ResearchArea,
                ProfessorInHCIorAI, FacultyPhoneBook, EE_Course, Schedule);
    }

    @Test
    public void shouldPassgetEquivalentClassesCS_Course() {
        equal(reasoner.getEquivalentClasses(CS_Course), assistantProfessor,
                aiStudent, aiDept, CS_Department, HCIStudent,
                CS_StudentTakingCourses, LecturerTaking4Courses, Lecturer,
                owlNothing, CS_Course);
    }

    @Test
    public void shouldPassgetDisjointClassesCS_Course() {
        equal(reasoner.getDisjointClasses(CS_Course), owlThing, Student,
                PhoneBook, CS_Student, UniversityPhoneBook, ResearchArea,
                TeachingFaculty, Department, Person, Professor, EE_Course,
                Faculty, EE_Library, assistantProfessor, aiStudent, aiDept,
                CS_Department, HCIStudent, CS_StudentTakingCourses,
                LecturerTaking4Courses, Lecturer, owlNothing, CS_Course,
                CS_Library, EE_Department, ProfessorInHCIorAI,
                FacultyPhoneBook, Library, Schedule, Course);
    }

    @Test
    public void shouldPassgetSubDataPropertieshasTenurefalse() {
        equal(reasoner.getSubDataProperties(hasTenure, false),
                owlbottomDataProperty);
    }

    @Test
    public void shouldPassgetSuperDataPropertieshasTenurefalse() {
        equal(reasoner.getSuperDataProperties(hasTenure, false),
                owltopDataProperty);
    }

    @Test
    public void shouldPassgetDataPropertyDomainshasTenurefalse() {
        equal(reasoner.getDataPropertyDomains(hasTenure, false), owlThing,
                Faculty, TeachingFaculty, Person, Professor);
    }

    @Test
    public void shouldPassgetSubDataPropertieshasTenuretrue() {
        equal(reasoner.getSubDataProperties(hasTenure, true),
                owlbottomDataProperty);
    }

    @Test
    public void shouldPassgetSuperDataPropertieshasTenuretrue() {
        equal(reasoner.getSuperDataProperties(hasTenure, true),
                owltopDataProperty);
    }

    @Test
    public void shouldPassgetDataPropertyDomainshasTenuretrue() {
        equal(reasoner.getDataPropertyDomains(hasTenure, true), Professor);
    }
}
