package com.oder.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.oder.data.local.entity.SrsStateEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SrsStateDao {

    @Query("SELECT * FROM srs_states WHERE wordId = :wordId")
    suspend fun getSrsStateByWordId(wordId: String): SrsStateEntity?

    @Query("SELECT * FROM srs_states WHERE wordId = :wordId")
    fun getSrsStateFlowByWordId(wordId: String): Flow<SrsStateEntity?>

    @Query("SELECT * FROM srs_states")
    fun getAllSrsStates(): Flow<List<SrsStateEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(srsState: SrsStateEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateAll(srsStates: List<SrsStateEntity>)

    @Update
    suspend fun update(srsState: SrsStateEntity)

    @Delete
    suspend fun delete(srsState: SrsStateEntity)

    @Query("DELETE FROM srs_states WHERE wordId = :wordId")
    suspend fun deleteByWordId(wordId: String)
}
