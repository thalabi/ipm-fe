package com.kerneldc.ipm.rest.controller;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.PagedModel;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.kerneldc.common.domain.AbstractEntity;
import com.kerneldc.common.enums.IEntityEnum;
import com.kerneldc.common.search.EntitySpecification;
import com.kerneldc.ipm.commonservices.enums.EntityEnumUtilities;
import com.kerneldc.ipm.commonservices.repository.EntityRepositoryFactory;

import jakarta.persistence.EntityManager;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/protected/genericEntityController")
@RequiredArgsConstructor
@Slf4j
public class GenericEntityController {

	private final EntityRepositoryFactory<?, ?> entityRepositoryFactory;
	private final EntityRepresentationModelAssembler entityRepresentationModelAssembler;
	private final EntityManager em;

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@GetMapping("/findAll")
	public ResponseEntity<PagedModel<AbstractEntity>> findAll(
			@RequestParam @NotNull String tableName, @RequestParam String search,
			Pageable pageable, @NotNull PagedResourcesAssembler<AbstractEntity> pagedResourcesAssembler) {

		IEntityEnum entityEnum = EntityEnumUtilities.getEntityEnum(tableName);
    	LOGGER.info("search: {}", search);
    	LOGGER.info("pageable: {}", pageable);
    	
    	var entityRepository = entityRepositoryFactory.getRepository(entityEnum);
    	var entityMetamodel = em.getMetamodel().entity(entityEnum.getEntity());
    	
    	var entitySpecification = new EntitySpecification<AbstractEntity>(entityMetamodel, search);
    	
		var page = entityRepository.findAll((Specification)entitySpecification, pageable);
        PagedModel<?> pagedModel; 
        if (! /* not */ page.hasContent()) {
        	pagedModel = pagedResourcesAssembler.toEmptyModel(page, entityEnum.getEntity());
        } else {
        	var link = WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(GenericEntityController.class).findAll(tableName, search, pageable, pagedResourcesAssembler)).withSelfRel();
        	pagedModel = pagedResourcesAssembler.toModel(page, entityRepresentationModelAssembler, link);
        }

		LOGGER.debug("pagedModel: {}", pagedModel);
		
		var response = ResponseEntity.ok((PagedModel<AbstractEntity>)pagedModel);
		
		LOGGER.debug("r: {}", response);
		return response;	
    }
}
