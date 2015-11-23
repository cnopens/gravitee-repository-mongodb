/**
 * Copyright (C) 2015 The Gravitee team (http://gravitee.io)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.gravitee.repository.mongodb.management;

import io.gravitee.repository.exceptions.TechnicalException;
import io.gravitee.repository.management.api.PageRepository;
import io.gravitee.repository.management.model.Page;
import io.gravitee.repository.mongodb.management.internal.model.PageMongo;
import io.gravitee.repository.mongodb.management.internal.page.PageMongoRepository;
import io.gravitee.repository.mongodb.management.mapper.GraviteeMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * @author Titouan COMPIEGNE
 */
@Component
public class MongoPageRepository implements PageRepository {

	private final static Logger logger = LoggerFactory.getLogger(MongoPageRepository.class);

	@Autowired
	private PageMongoRepository internalPageRepo;

	@Autowired
	private GraviteeMapper mapper;


	@Override
	public Collection<Page> findPublishedByApi(String apiName) throws TechnicalException {
		logger.debug("Find published pages by api {}", apiName);

		List<PageMongo> pages = internalPageRepo.findByApi(apiName);
		Set<Page> res = mapper.collection2set(pages, PageMongo.class, Page.class);

		logger.debug("Find published pages by api {} - Done", apiName);
		return res;
	}

	@Override
	public Collection<Page> findByApi(String apiName) throws TechnicalException {
		logger.debug("Find pages by api {}", apiName);

		List<PageMongo> pages = internalPageRepo.findByApi(apiName);
		Set<Page> res = mapper.collection2set(pages, PageMongo.class, Page.class);

		logger.debug("Find pages by api {} - Done", apiName);
		return res;
	}

	@Override
	public Optional<Page> findById(String name) throws TechnicalException {
		logger.debug("Find page by name [{}]", name);

		PageMongo page = internalPageRepo.findOne(name);
		Page res = mapper.map(page, Page.class);

		logger.debug("Find page by name [{}] - Done", name);
		return Optional.ofNullable(res);
	}

	@Override
	public Page create(Page page) throws TechnicalException {
		logger.debug("Create page [{}]", page.getName());
		
		PageMongo pageMongo = mapper.map(page, PageMongo.class);
		PageMongo createdPageMongo = internalPageRepo.insert(pageMongo);
		
		Page res = mapper.map(createdPageMongo, Page.class);
		
		logger.debug("Create page [{}] - Done", page.getName());
		
		return res;
	}

	@Override
	public Page update(Page page) throws TechnicalException {
		if(page == null || page.getName() == null){
			throw new IllegalStateException("Page to update must have a name");
		}
		
		// Search team by name
		PageMongo pageMongo = internalPageRepo.findOne(page.getName());
		
		if(pageMongo == null){
			throw new IllegalStateException(String.format("No page found with name [%s]", page.getName()));
		}
		
		try{
			//Update 
			pageMongo.setTitle(page.getTitle());
			pageMongo.setContent(page.getContent());
			pageMongo.setLastContributor(page.getLastContributor());
			pageMongo.setUpdatedAt(page.getUpdatedAt());
			
			PageMongo pageMongoUpdated = internalPageRepo.save(pageMongo);
			return mapper.map(pageMongoUpdated, Page.class);

		}catch(Exception e){
			
			logger.error("An error occured when updating page",e);
			throw new TechnicalException("An error occured when updating page");
		}
	}

	@Override
	public void delete(String name) throws TechnicalException {
		try{
			internalPageRepo.delete(name);
		}catch(Exception e){
			logger.error("An error occured when deleting page [{}]", name, e);
			throw new TechnicalException("An error occured when deleting page");
		}
	}

	@Override
	public Integer findMaxPageOrderByApiName(String apiName) throws TechnicalException {
		try{
			return internalPageRepo.findMaxPageOrderByApiName(apiName);
		}catch(Exception e){
			logger.error("An error occured when searching max order page for api name [{}]", apiName, e);
			throw new TechnicalException("An error occured when searching max order page for api name");
		}
	}
}