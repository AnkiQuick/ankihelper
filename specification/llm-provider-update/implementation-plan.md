# Implementation Plan: LLM Provider Update

## Project Overview

**Goal**: Update LLM provider configurations by removing Aliyun and adding Moonshot AI and BigModel

**Timeline**: 1-2 hours
**Complexity**: Low (string changes + configuration updates)
**Risk Level**: Low (no schema changes, backward compatible)

## Milestones

### Milestone 1: Code Updates (30 minutes)
- Update string resources (English and Chinese)
- Update LLMConfigEditorActivity provider mappings
- Verify code compiles

### Milestone 2: Testing (30 minutes)
- Manual testing of new providers
- Verification of existing configs
- Build and lint checks

### Milestone 3: Documentation (15 minutes)
- Update implementation summary
- Document test results
- Record any issues encountered

### Milestone 4: Finalization (15 minutes)
- Final code review
- Commit changes
- Push to repository

## Detailed Task List

### Phase 1: Preparation
- [x] Research Moonshot AI API documentation
- [x] Research BigModel/ZhipuAI API documentation
- [x] Analyze current LLM config implementation
- [x] Create technical specification
- [x] Create implementation plan
- [ ] Backup current working state

### Phase 2: String Resource Updates

#### Task 2.1: Update English strings
**File**: `app/src/main/res/values/strings.xml`
**Priority**: High
**Estimated Time**: 5 minutes

**Actions**:
1. Locate provider string definitions (around line 95-98)
2. Remove line: `<string name="provider_aliyun">Aliyun</string>`
3. Add line: `<string name="provider_moonshot">Moonshot</string>`
4. Add line: `<string name="provider_bigmodel">BigModel</string>`

**Expected Result**:
```xml
<string name="provider_custom">Custom</string>
<string name="provider_deepseek">DeepSeek</string>
<string name="provider_openai">OpenAI</string>
<string name="provider_moonshot">Moonshot</string>
<string name="provider_bigmodel">BigModel</string>
```

**Verification**:
- [ ] Aliyun string removed
- [ ] Moonshot string added
- [ ] BigModel string added
- [ ] XML file remains valid

---

#### Task 2.2: Update Chinese strings
**File**: `app/src/main/res/values-zh/strings.xml`
**Priority**: High
**Estimated Time**: 5 minutes

**Actions**:
1. Locate provider string definitions (around line 126-129)
2. Remove line: `<string name="provider_aliyun">阿里云</string>`
3. Add line: `<string name="provider_moonshot">月之暗面</string>`
4. Add line: `<string name="provider_bigmodel">智谱</string>`

**Expected Result**:
```xml
<string name="provider_custom">自定义</string>
<string name="provider_deepseek">DeepSeek</string>
<string name="provider_openai">OpenAI</string>
<string name="provider_moonshot">月之暗面</string>
<string name="provider_bigmodel">智谱</string>
```

**Verification**:
- [ ] Aliyun string removed (阿里云)
- [ ] Moonshot string added (月之暗面)
- [ ] BigModel string added (智谱)
- [ ] XML file remains valid

---

### Phase 3: Provider Configuration Updates

#### Task 3.1: Update LLMConfigEditorActivity
**File**: `app/src/main/java/com/lmyby/ankiquicker/ui/ai/LLMConfigEditorActivity.kt`
**Priority**: High
**Estimated Time**: 10 minutes

**Actions**:
1. Locate `initProviderData()` method (line 56)
2. Remove Aliyun provider entry (line 61):
   ```kotlin
   providerUrls[getString(R.string.provider_aliyun)] = "https://dashscope.aliyuncs.com"
   ```
3. Add Moonshot provider entry after OpenAI:
   ```kotlin
   providerUrls[getString(R.string.provider_moonshot)] = "https://api.moonshot.ai/v1"
   ```
4. Add BigModel provider entry after Moonshot:
   ```kotlin
   providerUrls[getString(R.string.provider_bigmodel)] = "https://open.bigmodel.cn/api/paas/v4"
   ```

**Expected Result**:
```kotlin
private fun initProviderData() {
    // Initialize predefined providers and their URLs
    providerUrls[getString(R.string.provider_custom)] = ""
    providerUrls[getString(R.string.provider_deepseek)] = "https://api.deepseek.com"
    providerUrls[getString(R.string.provider_openai)] = "https://api.openai.com"
    providerUrls[getString(R.string.provider_moonshot)] = "https://api.moonshot.ai/v1"
    providerUrls[getString(R.string.provider_bigmodel)] = "https://open.bigmodel.cn/api/paas/v4"

    // Create list of provider names
    providerNames.addAll(providerUrls.keys)
}
```

**Verification**:
- [ ] Aliyun entry removed
- [ ] Moonshot entry added with correct URL
- [ ] BigModel entry added with correct URL
- [ ] Method still compiles
- [ ] No Detekt violations

---

### Phase 4: Build and Lint Verification

#### Task 4.1: Run build
**Priority**: High
**Estimated Time**: 2 minutes

**Command**: `./gradlew assembleDebug`

**Expected Result**: `BUILD SUCCESSFUL`

**Verification**:
- [ ] No compilation errors
- [ ] No resource errors
- [ ] APK generated successfully

---

#### Task 4.2: Run Detekt
**Priority**: High
**Estimated Time**: 1 minute

**Command**: `./gradlew detekt`

**Expected Result**: All checks pass

**Verification**:
- [ ] No Detekt violations
- [ ] Code meets quality standards

---

### Phase 5: Manual Testing

#### Task 5.1: Test Moonshot provider
**Priority**: High
**Estimated Time**: 5 minutes

**Steps**:
1. Launch app
2. Navigate to AI Manager → LLM Manager
3. Click "+" to add new config
4. Select "Moonshot" from provider dropdown
5. Verify base URL auto-fills to `https://api.moonshot.ai/v1`
6. Verify base URL field is disabled (not editable)
7. Fill in:
   - Name: "Test Moonshot"
   - Model Name: "moonshot-v1-8k"
   - API Token: "test-token"
8. Save configuration
9. Verify config appears in list
10. Edit config and verify fields populated correctly
11. Verify provider spinner shows "Moonshot"

**Verification**:
- [ ] Moonshot appears in provider dropdown
- [ ] Base URL auto-fills correctly
- [ ] Config saves successfully
- [ ] Config loads correctly when editing

---

#### Task 5.2: Test BigModel provider
**Priority**: High
**Estimated Time**: 5 minutes

**Steps**:
1. Click "+" to add new config
2. Select "BigModel" from provider dropdown
3. Verify base URL auto-fills to `https://open.bigmodel.cn/api/paas/v4`
4. Verify base URL field is disabled
5. Fill in:
   - Name: "Test BigModel"
   - Model Name: "glm-4"
   - API Token: "test-token"
6. Save configuration
7. Verify config appears in list
8. Edit config and verify fields populated correctly
9. Verify provider spinner shows "BigModel"

**Verification**:
- [ ] BigModel appears in provider dropdown
- [ ] Base URL auto-fills correctly
- [ ] Config saves successfully
- [ ] Config loads correctly when editing

---

#### Task 5.3: Verify Aliyun removal
**Priority**: High
**Estimated Time**: 3 minutes

**Steps**:
1. Open provider dropdown in LLM Config Editor
2. Verify "Aliyun" or "阿里云" does NOT appear in list
3. Count providers in dropdown:
   - Custom
   - DeepSeek
   - OpenAI
   - Moonshot
   - BigModel
   - **Total: 5 providers**

**Verification**:
- [ ] Aliyun not visible in English UI
- [ ] 阿里云 not visible in Chinese UI
- [ ] Total provider count is 5

---

#### Task 5.4: Test existing Aliyun configs (if any)
**Priority**: Medium
**Estimated Time**: 3 minutes

**Prerequisite**: Create an Aliyun config before making changes (for testing)

**Steps**:
1. If existing Aliyun config present, open it for editing
2. Verify provider dropdown shows "Custom" (not "Aliyun")
3. Verify base URL shows original Aliyun URL
4. Verify base URL field is enabled (editable)
5. Verify config remains functional

**Verification**:
- [ ] Existing Aliyun configs load correctly
- [ ] Provider shown as "Custom"
- [ ] URL remains unchanged
- [ ] Config remains editable and saveable

---

#### Task 5.5: Test language switching
**Priority**: Medium
**Estimated Time**: 5 minutes

**Steps**:
1. Create a Moonshot config in English UI
2. Change app language to Chinese (Settings → Language → 中文)
3. Navigate back to LLM Manager
4. Verify Moonshot config name shows correctly
5. Edit Moonshot config
6. Verify provider dropdown shows "月之暗面" (Chinese)
7. Change app language back to English
8. Verify provider dropdown shows "Moonshot" (English)

**Verification**:
- [ ] Provider names localized correctly in Chinese
- [ ] Provider names localized correctly in English
- [ ] Configs remain functional across language changes

---

### Phase 6: Documentation

#### Task 6.1: Create implementation summary
**File**: `specification/llm-provider-update/implementation-summary.md`
**Priority**: Medium
**Estimated Time**: 10 minutes

**Content**:
- Technical decisions and rationale
- Code structure overview
- Challenges encountered and solutions
- Test results summary
- Screenshots (if applicable)

**Verification**:
- [ ] Summary document created
- [ ] All decisions documented
- [ ] Test results recorded

---

#### Task 6.2: Update technical spec (if needed)
**File**: `specification/llm-provider-update/technical-spec.md`
**Priority**: Low
**Estimated Time**: 5 minutes

**Actions**:
- Add any discovered issues to spec
- Update API compatibility notes if needed
- Record any deviations from plan

**Verification**:
- [ ] Spec reflects actual implementation
- [ ] All issues documented

---

### Phase 7: Finalization

#### Task 7.1: Final code review
**Priority**: High
**Estimated Time**: 5 minutes

**Review Checklist**:
- [ ] All Aliyun references removed
- [ ] Moonshot provider added correctly
- [ ] BigModel provider added correctly
- [ ] String resources updated (both languages)
- [ ] Code follows Kotlin conventions
- [ ] No Detekt violations
- [ ] Build succeeds
- [ ] All tests passed

---

#### Task 7.2: Commit changes
**Priority**: High
**Estimated Time**: 5 minutes

**Commit Message**:
```
feat(llm): update LLM providers - remove Aliyun, add Moonshot and BigModel

- Remove Aliyun (阿里云) provider from configuration options
- Add Moonshot AI (月之暗面) provider with base URL https://api.moonshot.ai/v1
- Add BigModel/ZhipuAI (智谱) provider with base URL https://open.bigmodel.cn/api/paas/v4
- Update string resources for English and Chinese localizations
- Update LLMConfigEditorActivity provider mappings

Existing Aliyun configurations remain functional as custom providers.
No database migration required.

🤖 Generated with [Claude Code](https://claude.com/claude-code)

Co-Authored-By: Claude <noreply@anthropic.com>
```

**Actions**:
1. Stage all changes: `git add .`
2. Commit with message above
3. Verify commit created successfully

**Verification**:
- [ ] All files staged
- [ ] Commit message follows conventions
- [ ] Commit created successfully

---

#### Task 7.3: Push to repository
**Priority**: High
**Estimated Time**: 2 minutes

**Command**: `git push origin dev`

**Verification**:
- [ ] Push successful
- [ ] Changes visible on GitHub
- [ ] No push errors

---

## Checkpoint Reviews

### Checkpoint 1: After Code Updates
**Review Point**: After Phase 3 (Provider Configuration Updates)

**Questions**:
- [ ] Are all string resources updated correctly?
- [ ] Are provider URLs correct?
- [ ] Does code compile?
- [ ] Any unexpected issues?

**Decision**: Proceed to testing OR stop for human review

---

### Checkpoint 2: After Testing
**Review Point**: After Phase 5 (Manual Testing)

**Questions**:
- [ ] Do all tests pass?
- [ ] Are there any bugs?
- [ ] Is backward compatibility maintained?
- [ ] Any edge cases discovered?

**Decision**: Proceed to documentation OR stop for bug fixes

---

### Checkpoint 3: Before Commit
**Review Point**: After Phase 6 (Documentation)

**Questions**:
- [ ] Is documentation complete?
- [ ] Are all success metrics met?
- [ ] Any outstanding issues?
- [ ] Ready for commit?

**Decision**: Proceed to commit OR address issues

---

## Risk Mitigation

### Risk 1: Build Failures
**Likelihood**: Low
**Impact**: Medium

**Mitigation**:
- Run build after each code change
- Keep changes small and incremental
- Test in development branch first

**Contingency**:
- Revert changes if build fails
- Debug compilation errors
- Seek help if needed

---

### Risk 2: String Resource Errors
**Likelihood**: Low
**Impact**: Medium

**Mitigation**:
- Validate XML syntax
- Use IDE's XML validation
- Test both language variants

**Contingency**:
- Fix XML syntax errors
- Restore from backup if needed

---

### Risk 3: Existing Configs Break
**Likelihood**: Very Low
**Impact**: High

**Mitigation**:
- Test with existing Aliyun config
- Verify backward compatibility
- No database schema changes

**Contingency**:
- Revert changes immediately
- Investigate root cause
- Add migration logic if needed

---

## Success Criteria

### Must Have (Blocking)
- [x] Research completed
- [ ] Aliyun provider removed from UI
- [ ] Moonshot provider added and functional
- [ ] BigModel provider added and functional
- [ ] String resources updated for both languages
- [ ] Code compiles without errors
- [ ] Detekt checks pass
- [ ] Manual tests pass

### Should Have (Important)
- [ ] Documentation complete
- [ ] Implementation summary created
- [ ] Test results recorded
- [ ] Language switching verified

### Nice to Have (Optional)
- [ ] Screenshots captured
- [ ] API compatibility verified with real tokens
- [ ] Performance metrics recorded

---

## Dependencies

### Internal Dependencies
- LLMConfig data class (existing, no changes)
- AIConfigRepository (existing, no changes)
- LLMConfigEditorActivity (to be modified)
- String resources (to be modified)

### External Dependencies
- None (no external libraries or APIs)

### Build Dependencies
- Gradle build system
- Detekt linter
- Kotlin compiler

---

## Rollback Plan

If critical issues discovered:

1. **Immediate Actions**:
   - Stop all testing
   - Document the issue
   - Assess severity

2. **Rollback Steps**:
   ```bash
   git revert HEAD  # Revert last commit
   git push origin dev --force  # Push revert
   ```

3. **Alternative**: Manual file restoration
   - Restore `strings.xml` (English)
   - Restore `strings.xml` (Chinese)
   - Restore `LLMConfigEditorActivity.kt`
   - Rebuild and test

4. **Post-Rollback**:
   - Analyze root cause
   - Update implementation plan
   - Re-attempt with fixes

---

## Notes

- This is a **low-risk** change (no schema changes)
- Changes are **backward compatible** (existing configs work)
- Implementation should be **straightforward** (string + config updates)
- Testing should be **thorough** but **quick** (simple UI verification)

---

## Timeline Summary

| Phase | Estimated Time | Status |
|-------|---------------|--------|
| Preparation | 60 min | ✅ Complete |
| String Updates | 10 min | ⏳ Pending |
| Config Updates | 10 min | ⏳ Pending |
| Build & Lint | 3 min | ⏳ Pending |
| Manual Testing | 25 min | ⏳ Pending |
| Documentation | 15 min | ⏳ Pending |
| Finalization | 12 min | ⏳ Pending |
| **Total** | **135 min** | **45% Complete** |

---

## Agent Assignment Plan

### Development Agent
**Responsibilities**:
- Execute Phase 2 (String Resource Updates)
- Execute Phase 3 (Provider Configuration Updates)
- Execute Phase 4 (Build and Lint Verification)
- Apply code formatting
- Fix any compilation errors

**Deliverables**:
- Modified string resource files
- Modified LLMConfigEditorActivity
- Build success confirmation
- Detekt pass confirmation

---

### Testing Agent
**Responsibilities**:
- Execute Phase 5 (Manual Testing)
- Document test results
- Capture issues/bugs
- Verify backward compatibility

**Deliverables**:
- Test execution report
- Bug list (if any)
- Screenshots (if applicable)
- Compatibility verification

---

### Documentation Agent
**Responsibilities**:
- Execute Phase 6 (Documentation)
- Track implementation progress
- Create implementation summary
- Update technical spec if needed

**Deliverables**:
- implementation-summary.md
- Updated technical-spec.md
- Progress tracking document
- Final report

---

## Ready for Execution

All prerequisites met:
- [x] Research completed
- [x] Technical specification created
- [x] Implementation plan finalized
- [x] Success criteria defined
- [x] Risk mitigation planned
- [x] Agent responsibilities assigned

**Status**: ✅ Ready to begin Phase 2 (String Resource Updates)
