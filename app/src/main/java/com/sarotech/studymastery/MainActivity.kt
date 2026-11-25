
package com.sarotech.studymastery

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sarotech.studymastery.data.*
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID

private val Burgundy = Color(0xFF842844)
private val Gold = Color(0xFFF2C94C)
private val Night = Color(0xFF08090D)

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val dao = StudyDatabase.get(this).studyDao()

        setContent {
            val vm: StudyViewModel =
                viewModel(factory = StudyViewModel.factory(dao))

            StudyMasteryApp(vm)
        }
    }
}

class StudyViewModel(
    private val dao: StudyDao
) : ViewModel() {

    val subjects = dao.observeSubjects().stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        emptyList()
    )

    val topicCount = dao.observeTopicCount().stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        0
    )

    val completedCount = dao.observeCompletedCount().stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        0
    )

    fun topics(subjectId: String) =
        dao.observeTopics(subjectId)

    fun addSubject(name: String) = viewModelScope.launch {
        val clean = name.trim()

        if (clean.isNotEmpty()) {
            dao.insertSubject(
                SubjectEntity(
                    UUID.randomUUID().toString(),
                    clean,
                    "Custom subject",
                    false
                )
            )
        }
    }

    fun addTopic(subjectId: String, name: String) =
        viewModelScope.launch {

            val clean = name.trim()

            if (clean.isNotEmpty()) {
                dao.insertTopic(
                    TopicEntity(
                        UUID.randomUUID().toString(),
                        subjectId,
                        "Custom Topics",
                        clean,
                        "User-added topic"
                    )
                )
            }
        }

    fun toggle(topic: TopicEntity) = viewModelScope.launch {
        dao.updateTopicStatus(
            topic.id,
            if (topic.status == "COMPLETED") {
                "NOT_STARTED"
            } else {
                "COMPLETED"
            }
        )
    }

    companion object {

        fun factory(dao: StudyDao) =
            object : ViewModelProvider.Factory {

                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(
                    modelClass: Class<T>
                ): T {
                    return StudyViewModel(dao) as T
                }
            }
    }
}

@Composable
private fun StudyMasteryApp(vm: StudyViewModel) {

    val systemDark = isSystemInDarkTheme()

    var dark by remember {
        mutableStateOf(systemDark)
    }

    var selectedSubject by remember {
        mutableStateOf<SubjectEntity?>(null)
    }

    var showAddSubject by remember {
        mutableStateOf(false)
    }

    var showAddTopic by remember {
        mutableStateOf(false)
    }

    val subjects by vm.subjects.collectAsState()
    val total by vm.topicCount.collectAsState()
    val completed by vm.completedCount.collectAsState()

    val colors = if (dark) {
        darkColorScheme(
            primary = Gold,
            secondary = Burgundy,
            background = Night,
            surface = Color(0xFF17191F),
            onBackground = Color(0xFFF4F4F5),
            onSurface = Color(0xFFF4F4F5)
        )
    } else {
        lightColorScheme(
            primary = Burgundy,
            secondary = Gold,
            background = Color(0xFFF1F2F6),
            surface = Color.White,
            onBackground = Color(0xFF20212A),
            onSurface = Color(0xFF20212A)
        )
    }

    MaterialTheme(colorScheme = colors) {

        Scaffold(
            containerColor = MaterialTheme.colorScheme.background,

            topBar = {
                TopAppBarStudy(
                    title = selectedSubject?.name ?: "STUDY MASTERY",
                    onBack = { selectedSubject = null },
                    isDetail = selectedSubject != null,
                    dark = dark,
                    onTheme = { dark = !dark }
                )
            },

            floatingActionButton = {

                if (selectedSubject == null) {

                    FloatingActionButton(
                        onClick = { showAddSubject = true },
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = if (dark) Night else Color.White
                    ) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = "Add subject"
                        )
                    }

                } else {

                    FloatingActionButton(
                        onClick = { showAddTopic = true },
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = if (dark) Night else Color.White
                    ) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = "Add topic"
                        )
                    }
                }
            }

        ) { padding ->

            AnimatedContent(
                targetState = selectedSubject,
                transitionSpec = {
                    fadeIn(tween(180)) togetherWith
                            fadeOut(tween(120))
                },
                label = "screen"
            ) { subject ->

                if (subject == null) {

                    Dashboard(
                        Modifier.padding(padding),
                        subjects,
                        total,
                        completed,
                        onSelect = { selectedSubject = it }
                    )

                } else {

                    SubjectScreen(
                        Modifier.padding(padding),
                        subject,
                        vm
                    )
                }
            }
        }
    }

    if (showAddSubject) {

        NameDialog(
            title = "Add subject",
            label = "Subject name",
            onDismiss = { showAddSubject = false }
        ) {
            vm.addSubject(it)
            showAddSubject = false
        }
    }

    if (showAddTopic && selectedSubject != null) {

        NameDialog(
            title = "Add topic",
            label = "Topic name",
            onDismiss = { showAddTopic = false }
        ) {
            vm.addTopic(selectedSubject!!.id, it)
            showAddTopic = false
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TopAppBarStudy(
    title: String,
    onBack: () -> Unit,
    isDetail: Boolean,
    dark: Boolean,
    onTheme: () -> Unit
) {

    TopAppBar(

        title = {
            Column {

                Text(
                    title,
                    fontWeight = FontWeight.Bold
                )

                if (!isDetail) {
                    Text(
                        "BY SAROTECH  ·  LEARN • REMEMBER • ACHIEVE",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },

        navigationIcon = {

            if (isDetail) {

                IconButton(onClick = onBack) {
                    Icon(
                        Icons.Default.ArrowBack,
                        contentDescription = "Back"
                    )
                }

            } else {

                Icon(
                    Icons.Default.School,
                    contentDescription = null,
                    modifier = Modifier.padding(start = 16.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        },

        actions = {
            TextButton(onClick = onTheme) {
                Text(if (dark) "LIGHT" else "DARK")
            }
        },

        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.background
        )
    )
}

@Composable
private fun Dashboard(
    modifier: Modifier,
    subjects: List<SubjectEntity>,
    total: Int,
    completed: Int,
    onSelect: (SubjectEntity) -> Unit
) {

    val progress = if (total == 0) {
        0f
    } else {
        completed.toFloat() / total
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {

        item {

            Box(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(26.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(
                                Color(0xFF28212A),
                                Color(0xFF11131A)
                            )
                        )
                    )
                    .padding(20.dp)
            ) {

                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {

                    Text(
                        "YOUR LEARNING SPACE",
                        color = Gold,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        "Build knowledge.\nMake it memorable.",
                        color = Color.White,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        "A focused workspace for every subject.",
                        color = Color(0xFFD4D2D8),
                        style = MaterialTheme.typography.bodyMedium
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {

                        Box(Modifier.weight(1f)) {

                            LinearProgressIndicator(
                                progress = { progress },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(CircleShape),
                                color = Gold,
                                trackColor = Color(0xFF514B55)
                            )
                        }

                        Text(
                            "${(progress * 100).toInt()}%",
                            color = Gold,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Text(
                        "$completed of $total syllabus topics completed",
                        color = Color(0xFFD4D2D8),
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }
        }

        item {
            Text(
                "My Subjects",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        }

        items(subjects, key = { it.id }) { subject ->

            SubjectCard(subject) {
                onSelect(subject)
            }
        }

        if (subjects.isEmpty()) {

            item {
                Text(
                    "Preparing your study space…",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        item {

            Text(
                "Tip: Open a subject to track topics. Use + to add your own subjects and topics.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun SubjectCard(
    subject: SubjectEntity,
    onClick: () -> Unit
) {

    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(
                MaterialTheme.colorScheme.surface.copy(alpha = 0.78f)
            )
            .clickable(onClick = onClick)
            .padding(16.dp),

        verticalAlignment = Alignment.CenterVertically
    ) {

        Box(
            Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                ),
            contentAlignment = Alignment.Center
        ) {

            Icon(
                if (subject.id == "CHEMISTRY") {
                    Icons.Default.Science
                } else {
                    Icons.Default.MenuBook
                },
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
        }

        Spacer(Modifier.width(14.dp))

        Column(
            Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(3.dp)
        ) {

            Text(
                subject.name,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                subject.subtitle.ifBlank { "Custom subject" },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (subject.isOfficial) {

                Text(
                    "PRELOADED SYLLABUS",
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Icon(
            Icons.Default.ChevronRight,
            contentDescription = "Open subject",
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun SubjectScreen(
    modifier: Modifier,
    subject: SubjectEntity,
    vm: StudyViewModel
) {

    val topics by vm.topics(subject.id).collectAsState(
        initial = emptyList()
    )

    val grouped = topics.groupBy { it.part }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {

        item {

            Text(
                subject.subtitle,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodySmall
            )

            Text(
                "${topics.count { it.status == "COMPLETED" }} / ${topics.size} topics completed",
                fontWeight = FontWeight.SemiBold
            )
        }

        grouped.forEach { (part, partTopics) ->

            item {

                Text(
                    part,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            items(partTopics, key = { it.id }) { topic ->

                Column(
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .background(
                            MaterialTheme.colorScheme.surface.copy(alpha = 0.82f)
                        )
                        .padding(14.dp)
                ) {

                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {

                        Column(Modifier.weight(1f)) {

                            Text(
                                topic.name,
                                fontWeight = FontWeight.SemiBold
                            )

                            Text(
                                if (topic.status == "COMPLETED") {
                                    "Completed"
                                } else {
                                    "Not started"
                                },
                                color = if (topic.status == "COMPLETED") {
                                    Color(0xFF2E9D68)
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                },
                                style = MaterialTheme.typography.labelSmall
                            )
                        }

                        IconButton(
                            onClick = { vm.toggle(topic) }
                        ) {

                            Icon(
                                if (topic.status == "COMPLETED") {
                                    Icons.Default.CheckCircle
                                } else {
                                    Icons.Default.Book
                                },
                                contentDescription = "Toggle completion",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    Text(
                        topic.details,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 5,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
private fun NameDialog(
    title: String,
    label: String,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit
) {

    var value by remember {
        mutableStateOf("")
    }

    AlertDialog(

        onDismissRequest = onDismiss,

        title = {
            Text(title)
        },

        text = {

            OutlinedTextField(
                value = value,
                onValueChange = { value = it },
                label = { Text(label) },
                singleLine = true
            )
        },

        confirmButton = {

            TextButton(
                onClick = { onSave(value) },
                enabled = value.isNotBlank()
            ) {
                Text("Save")
            }
        },

        dismissButton = {

            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
