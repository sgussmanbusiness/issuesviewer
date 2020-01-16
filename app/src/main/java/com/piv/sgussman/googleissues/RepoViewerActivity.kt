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
import org.eclipse.egit.github.core.Repository
import org.eclipse.egit.github.core.client.PageIterator
import org.eclipse.egit.github.core.service.RepositoryService
import kotlin.concurrent.thread

// Implements OnScrollChangedListener so that the app can catch when the user reaches the end of the list
class RepoViewerActivity : AppCompatActivity(), View.OnScrollChangeListener{

    // Shared inter-thread GitHub networking resources
    var repoServ : RepositoryService? = null
    var pagIt : PageIterator<Repository>? = null

    // Static (shared) field for limiting firing of the 'can't-scroll-vertically' event
    companion object{
        var servicingCantScrollVertically = false
    }

    override fun onCreate(savedInstanceState: Bundle?){
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_repo_viewer)

        // Do networking away from the UI thread
        thread(true, false, null, "GitHubNetworking", -1){
            try{
                repoServ = RepositoryService()
                // Paginate Google repos
                pagIt = repoServ!!.pageRepositories("google", 1, 20)
                val curPage = pagIt!!.first()
                // Log.d("Task_2", "Size: " + curPage.size.toString())

                // Make changes to the UI back on the UI thread
                val repoListLinearLayout = findViewById<LinearLayout>(R.id.repoListLinearLayout)
                runOnUiThread{
                    // Add each repository as its own Button to the UI
                    for(i in curPage.indices){
                        val curRepoButton = Button(this)
                        curRepoButton.text = curPage.elementAt(i).name
                        // Log.d("OLDEST", (pagIt!!.nextPage - 2).toString())
                        // Minus 1 to get current page number, and minus 1 to get zero-indexed page number
                        curRepoButton.id = ((pagIt!!.nextPage - 2) * 20) + i
                        // Base the color of the button on the programming language of the repo project
                        if(curPage.elementAt(i).language == "Python"){
                            curRepoButton.setBackgroundColor(Color.parseColor("#199FFF")) // Color.BLUE
                            curRepoButton.setTextColor(Color.parseColor("#A3D8FF"))
                        }else if(curPage.elementAt(i).language == "C++"){
                            curRepoButton.setBackgroundColor(Color.parseColor("#FF9BDA")) // Color.MAGENTA
                            curRepoButton.setTextColor(Color.parseColor("#FFDBF1"))
                        }else if(curPage.elementAt(i).language == "Java"){
                            curRepoButton.setBackgroundColor(Color.parseColor("#CCAA92")) // Dark gray
                            curRepoButton.setTextColor(Color.parseColor("#EAE3DE"))
                        }else if(curPage.elementAt(i).language == "Go"){
                            curRepoButton.setBackgroundColor(Color.parseColor("#62C0C1")) // Cyan
                            curRepoButton.setTextColor(Color.parseColor("#CEEAEA"))
                        }else if(curPage.elementAt(i).language == "JavaScript"){
                            curRepoButton.setBackgroundColor(Color.parseColor("#C0C16E")) // Yellow
                            curRepoButton.setTextColor(Color.parseColor("#EAEADA"))
                        }else{
                            curRepoButton.setBackgroundColor(Color.parseColor("#93CE71")) // Light gray
                            curRepoButton.setTextColor(Color.parseColor("#D7EACC"))
                        }
                        curRepoButton.setOnClickListener{onAnyRepoButtonPressed(curRepoButton)}
                        // curRepoButton.height = 70
                        // curRepoButton.setPadding(0, 20, 0, 20)
                        repoListLinearLayout.addView(curRepoButton)
                    }
                }

            }catch(e : Exception){
                Log.e("Initial_GitHub_Thread", e.toString())
            }
        }

        // Subscribe to receive onScrollChange events
        val scrollView = findViewById<ScrollView>(R.id.repoScrollView)
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
                    if(pagIt!!.hasNext()) {
                        val curPage = pagIt!!.next()

                        // Make changes to the UI back on the UI thread
                        val repoListLinearLayout =
                            findViewById<LinearLayout>(R.id.repoListLinearLayout)
                        runOnUiThread {
                            // Add each repository as its own Button to the UI
                            for (i in curPage.indices) {
                                val curRepoButton = Button(this)
                                curRepoButton.text = curPage.elementAt(i).name
                                // Minus 1 to get current page number, and minus 1 to get zero-indexed page number
                                // TO-DO: What happens when nextPage is checked on the final page?
                                curRepoButton.id = ((pagIt!!.nextPage - 2) * 20) + i
                                // Base the color of the button on the programming language of the repo project
                                if(curPage.elementAt(i).language == "Python"){
                                    curRepoButton.setBackgroundColor(Color.parseColor("#199FFF")) // Color.BLUE
                                    curRepoButton.setTextColor(Color.parseColor("#A3D8FF"))
                                }else if(curPage.elementAt(i).language == "C++"){
                                    curRepoButton.setBackgroundColor(Color.parseColor("#FF9BDA")) // Color.MAGENTA
                                    curRepoButton.setTextColor(Color.parseColor("#FFDBF1"))
                                }else if(curPage.elementAt(i).language == "Java"){
                                    curRepoButton.setBackgroundColor(Color.parseColor("#CCAA92")) // Dark gray
                                    curRepoButton.setTextColor(Color.parseColor("#EAE3DE"))
                                }else if(curPage.elementAt(i).language == "Go"){
                                    curRepoButton.setBackgroundColor(Color.parseColor("#62C0C1")) // Cyan
                                    curRepoButton.setTextColor(Color.parseColor("#CEEAEA"))
                                }else if(curPage.elementAt(i).language == "JavaScript"){
                                    curRepoButton.setBackgroundColor(Color.parseColor("#C0C16E")) // Yellow
                                    curRepoButton.setTextColor(Color.parseColor("#EAEADA"))
                                }else{
                                    curRepoButton.setBackgroundColor(Color.parseColor("#93CE71")) // Light gray
                                    curRepoButton.setTextColor(Color.parseColor("#D7EACC"))
                                }
                                curRepoButton.setOnClickListener{onAnyRepoButtonPressed(curRepoButton)}
                                // curRepoButton.height = 70
                                // curRepoButton.setPadding(0, 20, 0, 20)
                                repoListLinearLayout.addView(curRepoButton)
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

    // Event callback for raised repo Button presses
    fun onAnyRepoButtonPressed(view : View){
        // Log.d("Button Pressed!", view.id.toString())

        // Get the reference to the actual Button
        val button = findViewById<Button>(view.id)

        // Start the IssueViewerActivity
        val intent = Intent(this, IssueViewerActivity::class.java).apply{
            // TO-DO: it would be better if an enum (int) were used for the key
            // Send the name of the clicked repo over to the new Activity to know which issues to list
            putExtra("REPOSITORY_MESSAGE", button.text)
            // TO-DO: send over the programming language of the project so as to properly color the Activity's Views
        }
        startActivity(intent)
    }
}

// Deleted:
// val localOutput = repoServ.getRepositories("google")
// val netThreadHandler: Handler = Handler(Looper.getMainLooper())