package com.example.parser

import com.example.model.Question
import com.example.model.Quiz
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.util.UUID

sealed class ValidationResult {
    data class Success(val quiz: Quiz) : ValidationResult()
    data class Error(val errors: List<String>) : ValidationResult() {
        val errorMessage: String get() = errors.joinToString("\n• ", prefix = "• ")
    }
}

object QuizJsonParser {

    /**
     * Parses and validates raw JSON string into a Quiz object.
     * Returns ValidationResult.Success or ValidationResult.Error with human-readable error messages.
     */
    fun parseAndValidate(jsonString: String, existingId: String? = null): ValidationResult {
        val errors = mutableListOf<String>()
        val trimmed = jsonString.trim()

        if (trimmed.isEmpty()) {
            return ValidationResult.Error(listOf("JSON input cannot be empty."))
        }

        val jsonObject: JSONObject
        try {
            jsonObject = JSONObject(trimmed)
        } catch (e: JSONException) {
            return ValidationResult.Error(listOf("Invalid JSON syntax: ${e.localizedMessage ?: "Check formatting for missing braces or quotes."}"))
        }

        // Validate title
        val title = jsonObject.optString("title", "").trim()
        if (title.isEmpty()) {
            errors.add("Missing or empty 'title' field.")
        }

        val description = jsonObject.optString("description", "").trim()
        val category = jsonObject.optString("category", "General Knowledge").trim().ifEmpty { "General Knowledge" }
        val difficulty = jsonObject.optString("difficulty", "Medium").trim().ifEmpty { "Medium" }
        val timeLimit = jsonObject.optInt("timeLimit", 0).coerceAtLeast(0)
        val shuffleQuestions = jsonObject.optBoolean("shuffleQuestions", true)
        val shuffleOptions = jsonObject.optBoolean("shuffleOptions", true)
        val version = jsonObject.optInt("version", 1)

        // Validate questions array
        if (!jsonObject.has("questions")) {
            errors.add("Missing required 'questions' array.")
            return ValidationResult.Error(errors)
        }

        val questionsArray: JSONArray? = jsonObject.optJSONArray("questions")
        if (questionsArray == null || questionsArray.length() == 0) {
            errors.add("'questions' must be a non-empty array of questions.")
            return ValidationResult.Error(errors)
        }

        val questionList = mutableListOf<Question>()
        val seenIds = mutableSetOf<String>()

        for (i in 0 until questionsArray.length()) {
            val qIndex = i + 1
            val qObj = questionsArray.optJSONObject(i)
            if (qObj == null) {
                errors.add("Question #$qIndex is not a valid JSON object.")
                continue
            }

            var qId = qObj.optString("id", "").trim()
            if (qId.isEmpty()) {
                qId = "q_$qIndex"
            }

            if (seenIds.contains(qId)) {
                errors.add("Duplicate question ID '$qId' at Question #$qIndex.")
            } else {
                seenIds.add(qId)
            }

            val questionText = qObj.optString("question", "").trim()
            if (questionText.isEmpty()) {
                errors.add("Question #$qIndex has empty or missing 'question' text.")
            }

            val optionsArray = qObj.optJSONArray("options")
            if (optionsArray == null || optionsArray.length() < 2) {
                errors.add("Question #$qIndex must contain at least 2 options in 'options' array.")
                continue
            }

            val optionsList = mutableListOf<String>()
            for (j in 0 until optionsArray.length()) {
                val opt = optionsArray.optString(j, "").trim()
                if (opt.isEmpty()) {
                    errors.add("Question #$qIndex has an empty option at position ${j + 1}.")
                }
                optionsList.add(opt)
            }

            val answer = qObj.optInt("answer", -1)
            if (answer < 0 || answer >= optionsList.size) {
                errors.add("Question #$qIndex has invalid answer index '$answer'. It must be between 0 and ${optionsList.size - 1}.")
            }

            val points = qObj.optInt("points", 1).coerceAtLeast(1)
            val explanation = if (qObj.has("explanation")) qObj.optString("explanation", "").trim().ifEmpty { null } else null

            questionList.add(
                Question(
                    id = qId,
                    question = questionText,
                    options = optionsList,
                    answer = answer,
                    points = points,
                    explanation = explanation
                )
            )
        }

        if (errors.isNotEmpty()) {
            return ValidationResult.Error(errors)
        }

        val quiz = Quiz(
            id = existingId ?: UUID.randomUUID().toString(),
            title = title,
            description = description,
            category = category,
            difficulty = difficulty,
            timeLimit = timeLimit,
            shuffleQuestions = shuffleQuestions,
            shuffleOptions = shuffleOptions,
            version = version,
            questions = questionList,
            createdAt = System.currentTimeMillis()
        )

        return ValidationResult.Success(quiz)
    }

    /**
     * Serializes Quiz to standard, beautiful JSON format.
     */
    fun exportToJson(quiz: Quiz, questions: List<Question> = quiz.questions): String {
        val root = JSONObject()
        root.put("version", quiz.version)
        root.put("title", quiz.title)
        root.put("description", quiz.description)
        root.put("category", quiz.category)
        root.put("difficulty", quiz.difficulty)
        root.put("timeLimit", quiz.timeLimit)
        root.put("shuffleQuestions", quiz.shuffleQuestions)
        root.put("shuffleOptions", quiz.shuffleOptions)

        val questionsArray = JSONArray()
        for (q in questions) {
            val qObj = JSONObject()
            qObj.put("id", q.id)
            qObj.put("question", q.question)

            val opts = JSONArray()
            for (opt in q.options) {
                opts.put(opt)
            }
            qObj.put("options", opts)
            qObj.put("answer", q.answer)
            qObj.put("points", q.points)
            if (!q.explanation.isNullOrBlank()) {
                qObj.put("explanation", q.explanation)
            }
            questionsArray.put(qObj)
        }
        root.put("questions", questionsArray)

        return root.toString(2)
    }

    /**
     * Preloaded sample quizzes matching the categories and questions shown in the UI reference.
     */
    fun getSampleQuizzes(): List<Pair<Quiz, List<Question>>> {
        val samples = listOf(
            sampleGeneralKnowledgeJson to "general_knowledge_sample",
            sampleScienceJson to "science_quiz_sample",
            sampleMathJson to "math_practice_sample",
            sampleEnglishJson to "english_grammar_sample",
            sampleIctJson to "ict_basics_sample"
        )

        val result = mutableListOf<Pair<Quiz, List<Question>>>()
        for ((json, id) in samples) {
            when (val parsed = parseAndValidate(json, existingId = id)) {
                is ValidationResult.Success -> {
                    result.add(parsed.quiz to parsed.quiz.questions)
                }
                is ValidationResult.Error -> {
                    // Won't happen for tested samples
                }
            }
        }
        return result
    }

    const val sampleGeneralKnowledgeJson = """{
  "version": 1,
  "title": "General Knowledge",
  "description": "Explore diverse questions from world history, geography, and general facts.",
  "category": "General Knowledge",
  "difficulty": "Easy",
  "timeLimit": 300,
  "shuffleQuestions": true,
  "shuffleOptions": true,
  "questions": [
    {
      "id": "q1",
      "question": "What is the capital of Bangladesh?",
      "options": [
        "Dhaka",
        "Chittagong",
        "Khulna",
        "Rajshahi"
      ],
      "answer": 0,
      "points": 1,
      "explanation": "Dhaka is the capital and largest city of Bangladesh."
    },
    {
      "id": "q2",
      "question": "Which planet is known as the Red Planet?",
      "options": [
        "Earth",
        "Mars",
        "Jupiter",
        "Venus"
      ],
      "answer": 1,
      "points": 1,
      "explanation": "Mars is reddish due to the high amount of iron oxide on its surface."
    },
    {
      "id": "q3",
      "question": "Which is the largest ocean in the world?",
      "options": [
        "Atlantic Ocean",
        "Indian Ocean",
        "Pacific Ocean",
        "Arctic Ocean"
      ],
      "answer": 2,
      "points": 1,
      "explanation": "The Pacific Ocean covers more than 30% of Earth's surface."
    },
    {
      "id": "q4",
      "question": "Who wrote 'Romeo and Juliet'?",
      "options": [
        "Charles Dickens",
        "William Shakespeare",
        "Jane Austen",
        "Mark Twain"
      ],
      "answer": 1,
      "points": 1,
      "explanation": "William Shakespeare wrote the famous tragedy in the late 16th century."
    },
    {
      "id": "q5",
      "question": "In which country would you find the Great Pyramid of Giza?",
      "options": [
        "Greece",
        "Egypt",
        "Mexico",
        "Italy"
      ],
      "answer": 1,
      "points": 1,
      "explanation": "The Pyramids of Giza are located on the outskirts of Cairo, Egypt."
    }
  ]
}"""

    const val sampleScienceJson = """{
  "version": 1,
  "title": "Science Quiz",
  "description": "Test your fundamentals in physics, chemistry, and biology.",
  "category": "Science",
  "difficulty": "Medium",
  "timeLimit": 360,
  "shuffleQuestions": true,
  "shuffleOptions": true,
  "questions": [
    {
      "id": "s1",
      "question": "H2O is the chemical formula of?",
      "options": [
        "Hydrogen Peroxide",
        "Water",
        "Hydrochloric Acid",
        "Oxygen Gas"
      ],
      "answer": 1,
      "points": 1,
      "explanation": "H2O represents two hydrogen atoms bonded to one oxygen atom, forming water."
    },
    {
      "id": "s2",
      "question": "Which gas do green plants absorb during photosynthesis?",
      "options": [
        "Oxygen",
        "Carbon Dioxide",
        "Nitrogen",
        "Methane"
      ],
      "answer": 1,
      "points": 1,
      "explanation": "Plants take in carbon dioxide and water to produce glucose and release oxygen."
    },
    {
      "id": "s3",
      "question": "What is the hardest known natural mineral on Earth?",
      "options": [
        "Gold",
        "Iron",
        "Diamond",
        "Quartz"
      ],
      "answer": 2,
      "points": 1,
      "explanation": "Diamond rates 10 on the Mohs scale of mineral hardness."
    },
    {
      "id": "s4",
      "question": "Which organ in the human body pumps blood?",
      "options": [
        "Lungs",
        "Brain",
        "Heart",
        "Liver"
      ],
      "answer": 2,
      "points": 1,
      "explanation": "The heart is the muscular organ that pumps blood throughout the circulatory system."
    }
  ]
}"""

    const val sampleMathJson = """{
  "version": 1,
  "title": "Math Practice",
  "description": "Sharpen your arithmetic, algebra, and quick mental math skills.",
  "category": "Mathematics",
  "difficulty": "Hard",
  "timeLimit": 240,
  "shuffleQuestions": false,
  "shuffleOptions": true,
  "questions": [
    {
      "id": "m1",
      "question": "What is 5 + 7 = ?",
      "options": [
        "10",
        "11",
        "12",
        "13"
      ],
      "answer": 2,
      "points": 1,
      "explanation": "5 + 7 = 12."
    },
    {
      "id": "m2",
      "question": "What is the value of 12 × 12?",
      "options": [
        "124",
        "144",
        "154",
        "164"
      ],
      "answer": 1,
      "points": 2,
      "explanation": "12 squared is equal to 144."
    },
    {
      "id": "m3",
      "question": "What is the square root of 81?",
      "options": [
        "7",
        "8",
        "9",
        "11"
      ],
      "answer": 2,
      "points": 1,
      "explanation": "9 × 9 = 81."
    },
    {
      "id": "m4",
      "question": "If 2x + 6 = 16, what is the value of x?",
      "options": [
        "3",
        "5",
        "8",
        "10"
      ],
      "answer": 1,
      "points": 2,
      "explanation": "2x = 10, therefore x = 5."
    }
  ]
}"""

    const val sampleEnglishJson = """{
  "version": 1,
  "title": "English Grammar",
  "description": "Grammar, vocabulary, idioms, and sentence structures.",
  "category": "English",
  "difficulty": "Medium",
  "timeLimit": 300,
  "shuffleQuestions": true,
  "shuffleOptions": true,
  "questions": [
    {
      "id": "e1",
      "question": "Which of the following words is a noun?",
      "options": [
        "Quickly",
        "Happiness",
        "Bright",
        "Sing"
      ],
      "answer": 1,
      "points": 1,
      "explanation": "'Happiness' is an abstract noun indicating a state of being."
    },
    {
      "id": "e2",
      "question": "What is the past tense of the verb 'run'?",
      "options": [
        "Running",
        "Ran",
        "Runned",
        "Runs"
      ],
      "answer": 1,
      "points": 1,
      "explanation": "'Ran' is the irregular simple past tense of 'run'."
    },
    {
      "id": "e3",
      "question": "Which word is an antonym for 'Benevolent'?",
      "options": [
        "Malevolent",
        "Generous",
        "Kind",
        "Caring"
      ],
      "answer": 0,
      "points": 1,
      "explanation": "Malevolent means wishing evil or harm to others, the opposite of benevolent."
    }
  ]
}"""

    const val sampleIctJson = """{
  "version": 1,
  "title": "ICT Basics",
  "description": "Computer fundamentals, networking, web protocols, and hardware.",
  "category": "ICT",
  "difficulty": "Easy",
  "timeLimit": 300,
  "shuffleQuestions": true,
  "shuffleOptions": true,
  "questions": [
    {
      "id": "i1",
      "question": "What does CPU stand for?",
      "options": [
        "Central Processing Unit",
        "Computer Personal Utility",
        "Central Performance Unit",
        "Control Processing Unit"
      ],
      "answer": 0,
      "points": 1,
      "explanation": "CPU stands for Central Processing Unit, the main brain of a computer."
    },
    {
      "id": "i2",
      "question": "What protocol is used to secure browsing over the World Wide Web?",
      "options": [
        "FTP",
        "HTTP",
        "HTTPS",
        "SMTP"
      ],
      "answer": 2,
      "points": 1,
      "explanation": "HTTPS (Hypertext Transfer Protocol Secure) encrypts communication over the web."
    },
    {
      "id": "i3",
      "question": "What is the primary function of RAM?",
      "options": [
        "Permanent file storage",
        "Temporary volatile working memory",
        "Power distribution",
        "Display output"
      ],
      "answer": 1,
      "points": 1,
      "explanation": "RAM (Random Access Memory) provides high-speed volatile storage for active applications."
    }
  ]
}"""
}
