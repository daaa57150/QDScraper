package daaa.qdscraper.services.api.impl;

import java.util.List;

import daaa.qdscraper.Args;
import daaa.qdscraper.model.Game;
import daaa.qdscraper.model.Rom;
import daaa.qdscraper.services.api.ApiService;

/**
 * Utilities to query http://www.screenscraper.fr/ using its API
 * 
 * @author daaa
 *
 */
public class ScreenScraperApiService extends ApiService {

	@Override
	public List<Game> search(Rom rom, Args args) {
		// TODO implement screenscraper
		
		
		// pour chaque platforme (systemeid obligatoire, mame/75 pour arcade)
		// 1) req avec md5
		// 2) req avec nom-rom
		// 3) peut-être rechercher avec leur formulaire le nom du jeu
		
		return null;
	}

}
