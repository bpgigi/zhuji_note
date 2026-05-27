package com.zhuji.note.di

import android.content.Context
import androidx.room.Room
import com.zhuji.note.ai.DeepSeekClient
import com.zhuji.note.data.local.db.FolderDao
import com.zhuji.note.data.local.db.NoteDao
import com.zhuji.note.data.local.db.TagDao
import com.zhuji.note.data.local.db.ZhujiDatabase
import com.zhuji.note.data.local.preferences.UserPreferencesDataStore
import com.zhuji.note.data.repository.FolderRepositoryImpl
import com.zhuji.note.data.repository.NoteRepositoryImpl
import com.zhuji.note.data.repository.TagRepositoryImpl
import com.zhuji.note.domain.repository.FolderRepository
import com.zhuji.note.domain.repository.NoteRepository
import com.zhuji.note.domain.repository.TagRepository
import com.zhuji.note.domain.usecase.DeleteNoteUseCase
import com.zhuji.note.domain.usecase.GetNoteUseCase
import com.zhuji.note.domain.usecase.GetNotesUseCase
import com.zhuji.note.domain.usecase.PurgeTrashUseCase
import com.zhuji.note.domain.usecase.ReminderUseCase
import com.zhuji.note.domain.usecase.SaveNoteUseCase
import com.zhuji.note.domain.usecase.StatsUseCase
import com.zhuji.note.domain.usecase.ToggleFlagsUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides @Singleton
    fun provideDb(@ApplicationContext context: Context): ZhujiDatabase =
        Room.databaseBuilder(context, ZhujiDatabase::class.java, ZhujiDatabase.NAME)
            .fallbackToDestructiveMigration()
            .build()

    @Provides fun provideNoteDao(db: ZhujiDatabase): NoteDao = db.noteDao()
    @Provides fun provideTagDao(db: ZhujiDatabase): TagDao = db.tagDao()
    @Provides fun provideFolderDao(db: ZhujiDatabase): FolderDao = db.folderDao()

    @Provides @Singleton
    fun provideTagRepository(dao: TagDao): TagRepository = TagRepositoryImpl(dao)

    @Provides @Singleton
    fun provideFolderRepository(dao: FolderDao): FolderRepository = FolderRepositoryImpl(dao)

    @Provides @Singleton
    fun provideNoteRepository(noteDao: NoteDao, tagDao: TagDao): NoteRepository =
        NoteRepositoryImpl(noteDao, tagDao)

    @Provides @Singleton
    fun providePrefs(@ApplicationContext context: Context) = UserPreferencesDataStore(context)

    @Provides @Singleton
    fun provideJson(): Json = Json {
        ignoreUnknownKeys = true
        explicitNulls = false
        encodeDefaults = false
    }

    @Provides @Singleton
    fun provideOkHttp(): OkHttpClient {
        val log = HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BASIC }
        return OkHttpClient.Builder()
            .addInterceptor(log)
            .connectTimeout(20, TimeUnit.SECONDS)
            .readTimeout(120, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .build()
    }

    @Provides @Singleton
    fun provideAi(client: OkHttpClient, json: Json) = DeepSeekClient(client, json)

    // UseCases
    @Provides fun provideGetNotes(repo: NoteRepository) = GetNotesUseCase(repo)
    @Provides fun provideGetNote(repo: NoteRepository) = GetNoteUseCase(repo)
    @Provides fun provideSaveNote(repo: NoteRepository) = SaveNoteUseCase(repo)
    @Provides fun provideDeleteNote(repo: NoteRepository) = DeleteNoteUseCase(repo)
    @Provides fun provideToggleFlags(repo: NoteRepository) = ToggleFlagsUseCase(repo)
    @Provides fun provideReminder(repo: NoteRepository) = ReminderUseCase(repo)
    @Provides fun provideStats(repo: NoteRepository) = StatsUseCase(repo)
    @Provides fun providePurge(repo: NoteRepository) = PurgeTrashUseCase(repo)
}
