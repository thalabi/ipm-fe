package com.kerneldc.ipm.rest.controller;

import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.rest.webmvc.support.RepositoryEntityLinks;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.stereotype.Component;

import com.kerneldc.common.domain.AbstractEntity;

@Component
public class EntityRepresentationModelAssembler implements SimpleRepresentationModelAssembler<AbstractEntity> {

	@Autowired
	private RepositoryEntityLinks repositoryEntityLinks;


	@Override
	public void addLinks(EntityModel<AbstractEntity> resource) {
		var content = resource.getContent();
		Objects.requireNonNull(content, "Entity can not be null");
		Link link = repositoryEntityLinks.linkToItemResource(content, AbstractEntity.idExtractor);
		resource.add(link);
		resource.add(link.withSelfRel());
	}


	@Override
	public void addLinks(CollectionModel<EntityModel<AbstractEntity>> resources) {
		// NOOP
	}

}
