package com.piv.sgussman.googleissues

import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.core.view.children
import org.eclipse.egit.github.core.IRepositoryIdProvider
import org.eclipse.egit.github.core.Issue
import org.eclipse.egit.github.core.Repository
import org.eclipse.egit.github.core.RepositoryIssue
import org.eclipse.egit.github.core.client.PageIterator
import org.eclipse.egit.github.core.service.IssueService
import kotlin.concurrent.thread

// Implements OnScrollChangedListener so that the app can catch when the user reaches the end of the list
class IssueViewerActivity : AppCompatActivity(), View.OnScrollChangeListener{

    // Shared inter-thread GitHub networking resources
    var issueServ : IssueService? = null
    var pagIt : PageIterator<Issue>? = null // RepositoryIssue
    var repoName : String = ""

    // TO-DO: enum (int) would be preferable
    var filterMode = "all"

    // Static (shared) field for limiting firing of the 'can't-scroll-vertically' event
    companion object{
        var servicingCantScrollVertically = false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_issue_viewer)

        // Receive the name of the repository selected on the previous screen
        repoName = intent.getStringExtra("REPOSITORY_MESSAGE")
        val headerTextView = findViewById<TextView>(R.id.headerTextView)
        headerTextView.text = "  " + repoName + " Issues"

        // Do networking away from the UI thread
        thread(true, false, null, "GitHubNetworking", -1){
            try{
                issueServ = IssueService()
                // Paginate issues
                val mapArg : Map<String, String> = mapOf(IssueService.FILTER_STATE to "all")
                pagIt = issueServ!!.pageIssues("google", repoName, mapArg, 1, 20)
                val curPage = pagIt!!.first()
                // Log.d("Task_2", "Size: " + curPage.size.toString())

                // Make changes to the UI back on the UI thread
                val issueListLinearLayout = findViewById<LinearLayout>(R.id.issueListLinearLayout)
                runOnUiThread{
                    // Add each repository as its own Button to the UI
                    for(i in curPage.indices){
                        val curIssueButton = Button(this)
                        curIssueButton.text = curPage.elementAt(i).title
                        // Log.d("OLDEST", (pagIt!!.nextPage - 2).toString())
                        // Minus 1 to get current page number, and minus 1 to get zero-indexed page number
                        curIssueButton.id = ((pagIt!!.nextPage - 2) * 20) + i
                        // TO-DO: Send over state from the previous Activity so as to know which language (color)
                        // Base the color of the button on the programming language of the repo project
                        // if(curPage.elementAt(i).language == "Python")
                        curIssueButton.setBackgroundColor(Color.parseColor("#93CE71")) // LTGRAY
                        curIssueButton.setTextColor(Color.parseColor("#D7EACC"))
                        // curRepoButton.height = 70
                        // curRepoButton.setPadding(0, 20, 0, 20)
                        issueListLinearLayout.addView(curIssueButton)
                    }
                }

            }catch(e : Exception){
                Log.e("Initial_GitHub_Thread", e.toString())
            }
        }

        // Subscribe to receive onScrollChange events
        val scrollView = findViewById<ScrollView>(R.id.issueScrollView)
        scrollView.setOnScrollChangeListener(this)
    }

    // Event callback for raised onScrollChange events
    override fun onScrollChange(v : View, scrollX : Int, scrollY : Int, oldScrollX : Int, oldScrollY : Int){
        // If the bottom of the ScrollView is reached, paginate more repos into the UI
        if(!RepoViewerActivity.servicingCantScrollVertically && !v.canScrollVertically(1)){
            RepoViewerActivity.servicingCantScrollVertically = true

            // Do networking away from the UI thread
            thread(true, false, null, "GitHubNetworking", -1){
                try{
                    if(pagIt!!.hasNext()){
                        val curPage = pagIt!!.next()

                        // Make changes to the UI back on the UI thread
                        val issueListLinearLayout =
                            findViewById<LinearLayout>(R.id.issueListLinearLayout)
                        runOnUiThread{
                            // Add each repository as its own Button to the UI
                            for (i in curPage.indices) {
                                val curIssueButton = Button(this)
                                curIssueButton.text = curPage.elementAt(i).title
                                // Minus 1 to get current page number, and minus 1 to get zero-indexed page number
                                // TO-DO: What happens when nextPage is checked on the final page?
                                curIssueButton.id = ((pagIt!!.nextPage - 2) * 20) + i
                                // Base the color of the button on the programming language of the repo project
                                curIssueButton.setBackgroundColor(Color.parseColor("#93CE71")) // LTGRAY
                                curIssueButton.setTextColor(Color.parseColor("#D7EACC"))
                                // curRepoButton.height = 70
                                // curRepoButton.setPadding(0, 20, 0, 20)
                                issueListLinearLayout.addView(curIssueButton)
                            }
                        }
                    }

                }catch(e : Exception){
                    Log.d("Later_GitHub_Thread", e.toString())
                }
                RepoViewerActivity.servicingCantScrollVertically = false;
            }
        }
    }

    // Event callback for filter button presses
    fun onAnyFilterButtonPressed(view : View){
        // Get the reference to the actual Button
        val button = findViewById<Button>(view.id)
        var changed = false

        val allButton = findViewById<Button>(R.id.allButton)
        val openButton = findViewById<Button>(R.id.openButton)
        val closedButton = findViewById<Button>(R.id.closedButton)

        if(filterMode != "all" && button.text == "All"){
            changed = true
            filterMode = "all"
            allButton.setBackgroundColor(Color.parseColor("#777777"))
            openButton.setBackgroundColor(Color.parseColor("#CCCCCC"))
            closedButton.setBackgroundColor(Color.parseColor("#CCCCCC"))

        }else if(filterMode != "open" && button.text == "Open"){
            changed = true
            filterMode = "open"
            allButton.setBackgroundColor(Color.parseColor("#CCCCCC"))
            openButton.setBackgroundColor(Color.parseColor("#777777"))
            closedButton.setBackgroundColor(Color.parseColor("#CCCCCC"))

        }else if(filterMode != "closed" && button.text == "Closed"){
            changed = true
            filterMode = "closed"
            allButton.setBackgroundColor(Color.parseColor("#CCCCCC"))
            openButton.setBackgroundColor(Color.parseColor("#CCCCCC"))
            closedButton.setBackgroundColor(Color.parseColor("#777777"))
        }

        // Delete all of the old Views from the hierarchy so make way for the new filtered data
        val issueListLinearLayout = findViewById<LinearLayout>(R.id.issueListLinearLayout)
        issueListLinearLayout.removeAllViews()

        // Populate the list based on the new filter
        // Do networking away from the UI thread
        thread(true, false, null, "GitHubNetworking", -1){
            try{
                val mapArg : Map<String, String> = mapOf(IssueService.FILTER_STATE to filterMode)
                pagIt = issueServ!!.pageIssues("google", repoName, mapArg, 1, 20)
                val curPage = pagIt!!.first()
                // Log.d("Task_2", "Size: " + curPage.size.toString())

                // Make changes to the UI back on the UI thread
                val issueListLinearLayout = findViewById<LinearLayout>(R.id.issueListLinearLayout)
                runOnUiThread{
                    // Add each repository as its own Button to the UI
                    for(i in curPage.indices){
                        val curIssueButton = Button(this)
                        curIssueButton.text = curPage.elementAt(i).title
                        // Log.d("OLDEST", (pagIt!!.nextPage - 2).toString())
                        // Minus 1 to get current page number, and minus 1 to get zero-indexed page number
                        curIssueButton.id = ((pagIt!!.nextPage - 2) * 20) + i
                        // TO-DO: Send over state from the previous Activity so as to know which language (color)
                        // Base the color of the button on the programming language of the repo project
                        // if(curPage.elementAt(i).language == "Python")
                        curIssueButton.setBackgroundColor(Color.parseColor("#93CE71")) // LTGRAY
                        curIssueButton.setTextColor(Color.parseColor("#D7EACC"))
                        // curRepoButton.height = 70
                        // curRepoButton.setPadding(0, 20, 0, 20)
                        issueListLinearLayout.addView(curIssueButton)
                    }
                }

            }catch(e : Exception){
                Log.e("Initial_GitHub_Thread", e.toString())
            }
        }
    }
}
