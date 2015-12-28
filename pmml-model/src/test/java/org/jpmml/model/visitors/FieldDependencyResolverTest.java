/*
 * Copyright (c) 2015 Villu Ruusmann
 */
package org.jpmml.model.visitors;

import java.io.InputStream;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

import javax.xml.transform.stream.StreamSource;

import org.dmg.pmml.DataField;
import org.dmg.pmml.DerivedField;
import org.dmg.pmml.Field;
import org.dmg.pmml.FieldName;
import org.dmg.pmml.OutputField;
import org.dmg.pmml.PMML;
import org.dmg.pmml.Visitor;
import org.dmg.pmml.VisitorAction;
import org.jpmml.model.FieldNameUtil;
import org.jpmml.model.FieldUtil;
import org.jpmml.model.JAXBUtil;
import org.jpmml.model.PMMLUtil;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class FieldDependencyResolverTest {

	@Test
	public void resolve() throws Exception {
		PMML pmml;

		try(InputStream is = PMMLUtil.getResourceAsStream(FieldResolverTest.class)){
			pmml = JAXBUtil.unmarshalPMML(new StreamSource(is));
		}

		FieldDependencyResolver resolver = new FieldDependencyResolver();
		resolver.applyTo(pmml);

		final
		Map<Field, Set<Field>> dependencies = resolver.getDependencies();

		Visitor visitor = new AbstractVisitor(){

			@Override
			public VisitorAction visit(DataField dataField){
				checkFields(Collections.<FieldName>emptySet(), dependencies.get(dataField));

				return super.visit(dataField);
			}

			@Override
			public VisitorAction visit(DerivedField derivedField){
				Set<Field> fields = dependencies.get(derivedField);

				FieldName name = derivedField.getName();

				if("x1_squared".equals(name.getValue())){
					checkFields(FieldNameUtil.create("x1"), fields);
				} else

				if("x1_cubed".equals(name.getValue())){
					checkFields(FieldNameUtil.create("x1", "x1_squared"), fields);
				} else

				if("x2_squared".equals(name.getValue()) || "x2_cubed".equals(name.getValue())){
					checkFields(FieldNameUtil.create("x2"), fields);
				} else

				{
					throw new AssertionError();
				}

				return super.visit(derivedField);
			}

			@Override
			public VisitorAction visit(OutputField outputField){
				checkFields(Collections.<FieldName>emptySet(), dependencies.get(outputField));

				return super.visit(outputField);
			}
		};

		visitor.applyTo(pmml);
	}

	static
	private void checkFields(Set<FieldName> names, Set<Field> fields){
		assertEquals(names, FieldUtil.nameSet(fields));
	}
}