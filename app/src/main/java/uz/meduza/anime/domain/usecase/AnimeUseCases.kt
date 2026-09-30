package uz.meduza.anime.domain.usecase

import android.content.Context
import uz.meduza.anime.data.models.AnimeDto
import uz.meduza.anime.data.models.GenreDto
import uz.meduza.anime.data.models.RateAnimeResponse
import uz.meduza.anime.data.repository.AnimeRepository

class GetTrendingAnimesUseCase(context: Context) {
    private val repository = AnimeRepository(context)
    suspend operator fun invoke(limit: Int = 10): Result<List<AnimeDto>> = repository.getTrendingAnimes(limit)
}

class GetLatestAnimesUseCase(context: Context) {
    private val repository = AnimeRepository(context)
    suspend operator fun invoke(limit: Int = 10): Result<List<AnimeDto>> = repository.getLatestAnimes(limit)
}

class GetAnimeDetailUseCase(context: Context) {
    private val repository = AnimeRepository(context)
    suspend operator fun invoke(slug: String): Result<AnimeDto> = repository.getAnimeDetail(slug)
}

class SearchAnimesUseCase(context: Context) {
    private val repository = AnimeRepository(context)
    suspend operator fun invoke(query: String, genre: String? = null, year: Int? = null): Result<List<AnimeDto>> =
        repository.searchAnimes(query, genre, year)
}

class RateAnimeUseCase(context: Context) {
    private val repository = AnimeRepository(context)
    suspend operator fun invoke(slug: String, score: Int): Result<RateAnimeResponse> =
        repository.rateAnime(slug, score)
}

class GetGenresUseCase(context: Context) {
    private val repository = AnimeRepository(context)
    suspend operator fun invoke(): Result<List<GenreDto>> = repository.getGenres()
}
