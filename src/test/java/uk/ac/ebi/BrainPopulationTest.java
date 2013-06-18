/**
 * 
 */
package uk.ac.ebi;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import uk.ac.ebi.brain.core.Brain;
import uk.ac.ebi.brain.error.BadNameException;
import uk.ac.ebi.brain.error.BadPrefixException;
import uk.ac.ebi.brain.error.BrainException;
import uk.ac.ebi.brain.error.ClassExpressionException;
import uk.ac.ebi.brain.error.DataPropertyExpressionException;
import uk.ac.ebi.brain.error.ExistingClassException;
import uk.ac.ebi.brain.error.ExistingDataPropertyException;
import uk.ac.ebi.brain.error.ExistingEntityException;
import uk.ac.ebi.brain.error.ExistingNamedIndividualException;
import uk.ac.ebi.brain.error.ExistingObjectPropertyException;
import uk.ac.ebi.brain.error.NewOntologyException;
import uk.ac.ebi.brain.error.NonExistingEntityException;
import uk.ac.ebi.brain.error.ObjectPropertyExpressionException;

/**
 * @author Samuel Croset
 *
 */
public class BrainPopulationTest {

	Brain brain;

	@Before
	public void bootstrap() throws BrainException {
		brain = new Brain();
	}

	@After
	public void dispose() {
		brain.sleep();
	}

	@Test(expected = BadPrefixException.class)
	public void wrongPrefixTest() throws BrainException {
		brain = new Brain("htt://www.example.org/", "http://www.example.org/demo.owl");
	}

	@Test
	public void addClassTest() throws BrainException{
		brain.addClass("A");
		assertEquals(true, brain.getOntology().containsClassInSignature(IRI.create(brain.getPrefixManager().getDefaultPrefix() + "A")));
	}

	@Test
	public void addExternalClassTest() throws BrainException{
		brain.addClass("http://www.example.org/A");
		assertEquals(true, brain.getOntology().containsClassInSignature(IRI.create("http://www.example.org/A")));
		assertNotNull(brain.getOWLClass("A"));
	}

	@Test(expected = BadNameException.class)
	public void addClassWithBadNameTest() throws BrainException {
		brain.addClass("Blood Coagulation");
	}

	@Test(expected = ExistingClassException.class)
	public void addExistingClassTest() throws BrainException{
		brain.addClass("A");
		assertEquals(true, brain.getOntology().containsClassInSignature(IRI.create(brain.getPrefixManager().getDefaultPrefix() + "A")));
		brain.addClass("A");
	}

	@Test
	public void getOwlClassTest() throws BrainException {
		OWLClass owlClassA = brain.addClass("A");
		OWLClass owlClassAprime = brain.getOWLClass("A");
		assertEquals(owlClassA, owlClassAprime);
	}

	@Test(expected = NonExistingEntityException.class)
	public void getNonExsistingOwlClassTest() throws NonExistingEntityException {
		brain.getOWLClass("A");
	}

	@Test
	public void subClassOfTest() throws BrainException {
		brain.addClass("A");
		brain.addClass("B");
		brain.subClassOf("A", "B");
	}

	@Test
	public void equivalentClassesTest() throws BrainException{
		brain.addClass("A");
		brain.addClass("B");
		brain.equivalentClasses("A", "B");
	}

	@Test
	public void disjointClassesTest() throws BrainException{
		brain.addClass("A");
		brain.addClass("B");
		brain.disjointClasses("A", "B");
	}

	@Test(expected = ClassExpressionException.class)
	public void subClassOfErrorTest() throws ClassExpressionException{
		brain.subClassOf("A", "C");
	}

	@Test
	public void saveOntology() throws BrainException {
		brain.addClass("A");
		brain.addClass("B");
		brain.subClassOf("A", "B");
		brain.save("src/test/resources/output.owl");
	}

	@Test
	public void addExternalIndividualTest() throws BrainException {
		brain.addNamedIndividual("http://www.example.org/a");
		assertEquals(true, brain.getOntology().containsIndividualInSignature(IRI.create("http://www.example.org/a")));
		assertNotNull(brain.getOWLNamedIndividual("a"));
	}

	@Test(expected = BadNameException.class)
	public void addNamedIndividualWithBadNameTest() throws BrainException {
		brain.addClass("pouet pouet");
	}

	@Test(expected = ExistingNamedIndividualException.class)
	public void addExistingNamedIndividualTest() throws BrainException{
		brain.addNamedIndividual("a");
		assertEquals(true, brain.getOntology().containsIndividualInSignature(IRI.create(brain.getPrefixManager().getDefaultPrefix() + "a")));
		brain.addNamedIndividual("a");
	}

	@Test
	public void getOwlNamedIndividualTest() throws BrainException {
		OWLNamedIndividual owlClassA = brain.addNamedIndividual("a");
		OWLNamedIndividual owlClassAprime = brain.getOWLNamedIndividual("a");
		assertEquals(owlClassA, owlClassAprime);
	}

	@Test(expected = NonExistingEntityException.class)
	public void getNonExsistingOwlNamedIndividualTest() throws NonExistingEntityException {
		brain.getOWLNamedIndividual("a");
	}

	@Test
	public void individualAssertion() throws BrainException {
		Brain brain = new Brain();
		brain.addNamedIndividual("Joe");
		brain.addClass("Human");
		brain.addClass("Scientist");
		brain.type("Human and Scientist", "Joe");
		brain.getInstances("Human", false);
		assertEquals(1, brain.getInstances("Human", false).size());
	}

	@Test
	public void objectPropertyAssertionTest() throws BrainException {
		Brain brain = new Brain();
		brain.addNamedIndividual("joe");
		brain.addNamedIndividual("mary");
		brain.addObjectProperty("knows");
		brain.objectPropertyAssertion("joe", "knows", "mary");
		List<String> instances = brain.getInstances("knows value mary", false);
		assertEquals(1, instances.size());
		brain.transitive("knows");
		brain.addNamedIndividual("bob");
		brain.objectPropertyAssertion("bob", "knows", "joe");
		List<String> instances2 = brain.getInstances("knows value mary", false);
		assertEquals(2, instances2.size());
	}

	@Test
	public void objectPropertyTest() throws BrainException {
		brain.addObjectProperty("part-of");
		assertEquals(true, brain.getOntology().containsObjectPropertyInSignature(IRI.create(brain.getPrefixManager().getDefaultPrefix() + "part-of")));
	}

	@Test(expected = ExistingObjectPropertyException.class)
	public void redundantObjectPropertyTest() throws BrainException {
		brain.addObjectProperty("part-of");
		assertEquals(true, brain.getOntology().containsObjectPropertyInSignature(IRI.create(brain.getPrefixManager().getDefaultPrefix() + "part-of")));
		brain.addObjectProperty("part-of");
	}

	@Test
	public void addExternalObjectPropertyTest() throws BrainException{
		brain.addObjectProperty("http://www.example.org/part-of");
		assertEquals(true, brain.getOntology().containsObjectPropertyInSignature(IRI.create("http://www.example.org/part-of")));
		assertNotNull(brain.getOWLObjectProperty("part-of"));
	}

	@Test
	public void transitivePropertyTest() throws BrainException {
		brain.addObjectProperty("part-of");
		brain.transitive("part-of");
		brain.save("src/test/resources/output.owl");
	}

	@Test(expected = ObjectPropertyExpressionException.class)
	public void unknownTransitivePropertyTest() throws BrainException {
		brain.addObjectProperty("part-of");
		brain.transitive("regulates");
	}

	@Test
	public void reflexivePropertyTest() throws BrainException {
		brain.addObjectProperty("part-of");
		brain.reflexive("part-of");
		brain.save("src/test/resources/output.owl");
	}

	@Test
	public void dataPropertyTest() throws BrainException {
		brain.addDataProperty("has-age");
		assertEquals(true, brain.getOntology().containsDataPropertyInSignature(IRI.create(brain.getPrefixManager().getDefaultPrefix() + "has-age")));
		brain.save("src/test/resources/output.owl");
	}

	@Test(expected = ExistingDataPropertyException.class)
	public void existingDataPropertyTest() throws BrainException {
		brain.addDataProperty("has-age");
		brain.addDataProperty("has-age");
	}

	@Test
	public void addExternalDataPropertyTest() throws BrainException{
		brain.addDataProperty("http://www.example.org/has-age");
		assertEquals(true, brain.getOntology().containsDataPropertyInSignature(IRI.create("http://www.example.org/has-age")));
		assertNotNull(brain.getOWLDataProperty("has-age"));
	}

	@Test
	public void functionalDataPropertyTest() throws BrainException {
		brain.addDataProperty("has-age");
		brain.functional("has-age");
	}

	@Test(expected = DataPropertyExpressionException.class)
	public void functionalDataPropertyAssertionTest() throws BrainException{
		brain.addObjectProperty("part-of");
		brain.addDataProperty("has-age");
		brain.functional("part-of");
	}

	@Test
	public void domainTest() throws BrainException{
		brain.addClass("A");
		brain.addObjectProperty("part-of");
		brain.domain("part-of", "A");
	}

	@Test
	public void complexDomainTest() throws BrainException{
		brain.addClass("A");
		brain.addClass("B");
		brain.addDataProperty("has-age");
		brain.addObjectProperty("part-of");
		brain.domain("has-age", "A and B");
		brain.domain("part-of", "A and B");
		brain.save("src/test/resources/output.owl");
	}

	@Test
	public void rangeObjectPropertyTest() throws BrainException{
		brain.addClass("A");
		brain.addObjectProperty("part-of");
		brain.range("part-of", "A");
	}

	@Test
	public void rangeDataPropertyTest() throws BrainException {
		brain.addClass("A");
		brain.addDataProperty("has-age");
		brain.range("has-age", brain.INTEGER);
		brain.save("src/test/resources/output.owl");
	}

	@Test
	public void equivalentObjectPropertiesTest() throws BrainException {
		brain.addObjectProperty("part-of");
		brain.addObjectProperty("is-part-of");
		brain.equivalentProperties("part-of", "is-part-of");
	}

	@Test(expected = BrainException.class)
	public void wrongEquivalentObjectPropertiesTest() throws BrainException {
		brain.addObjectProperty("part-of");
		brain.addObjectProperty("is-part-of");
		brain.equivalentProperties("part-of", "isEEE-part-of");
	}

	@Test
	public void equivalentDataPropertiesTest() throws BrainException {
		brain.addDataProperty("has-age");
		brain.addDataProperty("number-of-years");
		brain.equivalentProperties("has-age", "number-of-years");
	}

	@Test(expected = BrainException.class)
	public void mixedEquivalentPropertiesTest() throws BrainException {
		brain.addDataProperty("has-age");
		brain.addObjectProperty("part-of");
		brain.equivalentProperties("has-age", "part-of");
	}

	@Test
	public void subObjectPropertyTest() throws BrainException{
		brain.addObjectProperty("positively-regulates");
		brain.addObjectProperty("regulates");
		brain.subPropertyOf("positively-regulates", "regulates");
		brain.save("src/test/resources/output.owl");
	}

	@Test
	public void subDataPropertyTest() throws BrainException{
		brain.addDataProperty("positively-regulates");
		brain.addDataProperty("regulates");
		brain.subPropertyOf("positively-regulates", "regulates");
		brain.save("src/test/resources/output.owl");
	}

	@Test(expected = BrainException.class)
	public void corruptedSubPropertyAseertionTest() throws BrainException{
		brain.addDataProperty("positively-regulates");
		brain.addObjectProperty("regulates");
		brain.subPropertyOf("positively-regulates", "regulates");
		brain.save("src/test/resources/output.owl");
	}

	@Test
	public void ChainedPropertiesTest() throws BrainException {
		brain.addObjectProperty("part-of");
		brain.addObjectProperty("regulates");
		brain.chain("regulates o part-of", "regulates");
		brain.save("src/test/resources/output.owl");
	}

	@Test(expected = ObjectPropertyExpressionException.class)
	public void wrongChainedPropertiesTest() throws BrainException {
		brain.addObjectProperty("part-of");
		brain.addObjectProperty("regulates");
		brain.chain("regulated o part-of", "regulates");
		brain.save("src/test/resources/output.owl");
	}

	@Test
	public void labelTest() throws BrainException {
		brain.addClass("A");
		brain.label("A", "this is the content of the label");
		brain.save("src/test/resources/output.owl");
	}

	@Test
	public void externalAnnotationTest() throws BrainException {
		brain.addAnnotationProperty("http://example.org/definition");
		brain.addClass("A");
		brain.annotation("A", "definition", "this is the definition of the entity");
	}

	@Test
	public void annotationTest() throws BrainException {
		brain.addAnnotationProperty("definition");
		brain.addClass("A");
		brain.annotation("A", "definition", "this is the definition of the entity");
		brain.save("src/test/resources/output.owl");
	}

	@Test(expected = NonExistingEntityException.class)
	public void wrongAnnotationTest() throws BrainException {
		brain.addAnnotationProperty("definition");
		brain.annotation("A", "definitionCorrupted", "this is the definition of the entity");
	}

	@Test
	public void builtInAnnotationTest() throws BrainException {
		brain.addClass("A");
		brain.comment("A", "his is a comment");
		brain.seeAlso("A", "see also there");
		brain.isDefinedBy("A", "is defined by that");
		brain.save("src/test/resources/output.owl");
	}

	@Test
	public void prefixTest() throws BrainException {
		Brain brain = new Brain("http://www.example.com/", "http://www.example.com/example.owl");
		brain.addClass("A");
		brain.addClass("http://www.example.com/B");
		brain.save("src/test/resources/prefix.owl");	
	}

	@Test
	public void contructorFromOntologyTest() throws OWLOntologyCreationException, ClassExpressionException, NewOntologyException, ExistingEntityException, BadPrefixException{
		OWLOntologyManager man = OWLManager.createOWLOntologyManager();
		OWLOntology ontology = man.loadOntologyFromOntologyDocument(new File("src/test/resources/dev.owl"));
		Brain brain = new Brain(ontology);
		List<String> subClasses = brain.getSubClasses("G", false);
		brain.sleep();
		assertEquals(3, subClasses.size());
	}

	@Test
	public void addClassBivalentHandling() throws BrainException {
		Brain brain = new Brain();
		brain.addClass("http://www.example.org/A");
		brain.addClass("B");
		brain.subClassOf("A", "B");
		brain.prefix("http://www.example.org/", "example");
		brain.save("src/test/resources/prefix.owl");
	}


}
