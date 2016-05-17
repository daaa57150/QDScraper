package daaa.qdscrapper.services;

import daaa.qdscrapper.Props;


// http://www.giantbomb.com/api/search/?api_key=xxxx&query=Metal%20Slug%20-%20Super%20Vehicle-001&resources=game&field_list=deck,description,id,image,name,original_release_date,platforms,api_detail_url
// => suivre api_detail_url
// http://www.giantbomb.com/api/game/3030-6941/?api_key=xxx&field_list=deck,description,id,image,name,original_release_date,developers,genres,publishers&format=json
/**
 * Utilities to query http://www.giantbomb.com/api using its API.
 * @see http://www.giantbomb.com/api/documentation
 * @author daaa
 *
 */
public class GiantBomb 
{
	private GiantBomb(){} // do not instanciate

	private static final String URL_GIANTBOMB_API = Props.get("giantbomb.url"); //"http://www.giantbomb.com/api/"; 
	private static final String GET_GAME = "search/?resources=game";
	private static final String DUPE_IMAGES_FOLDER = Props.get("dupe.images.folder");
	private static final String GIANTBOMB_API_ID = "GiantBomb";
	
	
}
