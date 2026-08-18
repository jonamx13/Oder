package com.oder.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.oder.data.local.entity.GrammarModuleEntity
import com.oder.data.local.entity.GrammarRuleEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface GrammarDao {

    // Grammar Modules
    @Query("SELECT * FROM grammar_modules")
    fun getAllModules(): Flow<List<GrammarModuleEntity>>

    @Query("SELECT * FROM grammar_modules WHERE language = :language")
    fun getModulesByLanguage(language: String): Flow<List<GrammarModuleEntity>>

    @Query("SELECT * FROM grammar_modules WHERE id = :id")
    suspend fun getModuleById(id: String): GrammarModuleEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertModule(module: GrammarModuleEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertModules(modules: List<GrammarModuleEntity>)

    @Update
    suspend fun updateModule(module: GrammarModuleEntity)

    @Delete
    suspend fun deleteModule(module: GrammarModuleEntity)

    // Grammar Rules
    @Query("SELECT * FROM grammar_rules WHERE moduleId = :moduleId")
    fun getRulesForModule(moduleId: String): Flow<List<GrammarRuleEntity>>

    @Query("SELECT * FROM grammar_rules WHERE targetWord = :targetWord")
    suspend fun getRulesForTargetWord(targetWord: String): List<GrammarRuleEntity>

    @Query("SELECT * FROM grammar_rules WHERE id = :id")
    suspend fun getRuleById(id: String): GrammarRuleEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRule(rule: GrammarRuleEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRules(rules: List<GrammarRuleEntity>)

    @Update
    suspend fun updateRule(rule: GrammarRuleEntity)

    @Delete
    suspend fun deleteRule(rule: GrammarRuleEntity)
}
