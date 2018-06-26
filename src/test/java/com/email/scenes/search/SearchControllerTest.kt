package com.email.scenes.search

import com.email.IHostActivity
import com.email.db.KeyValueStorage
import com.email.db.models.ActiveAccount
import com.email.mocks.MockedWorkRunner
import com.email.scenes.mailbox.MailboxTestUtils
import com.email.scenes.search.data.SearchDataSource
import com.email.scenes.search.data.SearchRequest
import com.email.scenes.search.data.SearchResult
import com.email.scenes.search.ui.SearchHistoryAdapter
import com.email.scenes.search.ui.SearchThreadAdapter
import com.email.scenes.search.ui.SearchUIObserver
import io.mockk.*
import org.amshove.kluent.`should be instance of`
import org.amshove.kluent.`should be`
import org.amshove.kluent.`should equal`
import org.junit.Before
import org.junit.Test

class SearchControllerTest{

    private lateinit var scene: SearchScene
    private lateinit var model: SearchSceneModel
    private lateinit var host: IHostActivity
    private lateinit var activeAccount: ActiveAccount
    private lateinit var storage: KeyValueStorage
    private lateinit var dataSource: SearchDataSource
    private lateinit var controller: SearchSceneController
    private lateinit var runner: MockedWorkRunner
    private lateinit var sentRequests: MutableList<SearchRequest>

    private val observerSlot = CapturingSlot<SearchUIObserver>()
    private val searchListenerSlot = CapturingSlot<SearchHistoryAdapter.OnSearchEventListener>()
    private val threadListenerSlot = CapturingSlot<SearchThreadAdapter.OnThreadEventListener>()
    private lateinit var listenerSlot: CapturingSlot<(SearchResult) -> Unit>

    private val queryText = "gabriel@criptext.com"
    private val anotherQueryText = "daniel@criptext.com"

    @Before
    fun setUp() {
        runner = MockedWorkRunner()
        scene = mockk(relaxed = true)
        model = SearchSceneModel()
        host = mockk(relaxed = true)
        dataSource = mockk(relaxed = true)
        storage = mockk(relaxed = true)
        activeAccount = ActiveAccount.fromJSONString(
                """ { "name":"Daniel","jwt":"_JWT_","recipientId":"daniel","deviceId":1} """)
        controller = SearchSceneController(
                scene = scene,
                model = model,
                host = host,
                activeAccount = activeAccount,
                storage = storage,
                dataSource = dataSource)

        listenerSlot = CapturingSlot()

        every {
            scene.attachView(any(), any(), capture(searchListenerSlot),
                    capture(threadListenerSlot), capture(observerSlot))
        } just Runs

        every {
            dataSource::listener.set(capture(listenerSlot))
        } just Runs

        sentRequests = mutableListOf()
        every { dataSource.submitRequest(capture(sentRequests)) } just Runs
        
    }

    @Test
    fun `on editText changed, should send SearchEmail request`() {

        controller.onStart(null)

        val observer = observerSlot.captured
        observer.onInputTextChange(queryText)

        model.queryText `should equal` queryText

        val sentRequest = sentRequests.first()
        sentRequest `should be instance of` SearchRequest.SearchEmails::class.java

    }

    @Test
    fun `on editText changed, the threads in model should be the threads that matched the queryText`() {

        controller.onStart(null)

        val observer = observerSlot.captured
        observer.onInputTextChange(queryText)

        model.queryText `should equal` queryText

        val threadsToSearch = MailboxTestUtils.createEmailThreads(20)

        listenerSlot.captured(SearchResult.SearchEmails.Success(emailThreads = threadsToSearch,
                isReset = false, queryText = queryText ))

        model.threads.size `should be` 20

    }

    @Test
    fun `on editText changed, if the queryText is different to the sent in request the result should be ignored`() {

        controller.onStart(null)

        val observer = observerSlot.captured
        observer.onInputTextChange(queryText)

        model.queryText `should equal` queryText

        val threadsToSearch = MailboxTestUtils.createEmailThreads(20)

        listenerSlot.captured(SearchResult.SearchEmails.Success(emailThreads = threadsToSearch,
                isReset = false, queryText = anotherQueryText ))

        model.threads.size `should be` 0

    }

}