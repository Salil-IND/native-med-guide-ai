package com.medguide.ai.data

data class EmergencyProtocol(
    val id: String,
    val title: String,
    val icon: String,
    val severity: Severity,
    val steps: List<String>,
    val warnings: List<String> = emptyList(),
    val callAmbulance: Boolean = false
)

data class FirstAidTopic(
    val id: String,
    val title: String,
    val icon: String,
    val category: String,
    val content: String,
    val steps: List<String>
)

data class DrugInfo(
    val name: String,
    val genericName: String,
    val uses: String,
    val dosageAdult: String,
    val dosageChild: String,
    val warnings: String,
    val sideEffects: String,
    val interactions: String
)

enum class Severity { CRITICAL, HIGH, MEDIUM, LOW }

object MedicalKnowledgeBase {

    val emergencyProtocols = listOf(
        EmergencyProtocol(
            id = "cardiac_arrest",
            title = "Cardiac Arrest / CPR",
            icon = "❤️",
            severity = Severity.CRITICAL,
            callAmbulance = true,
            steps = listOf(
                "1. CHECK for responsiveness — tap shoulders, shout 'Are you okay?'",
                "2. CALL 108 immediately or ask someone to call",
                "3. CHECK for breathing (no more than 10 seconds)",
                "4. START chest compressions: Place heel of hand on center of chest",
                "5. PUSH hard and fast — at least 2 inches deep, 100-120 times/minute",
                "6. GIVE rescue breaths (if trained): 30 compressions : 2 breaths",
                "7. CONTINUE until ambulance arrives or person starts breathing",
                "8. If AED available — use it immediately"
            ),
            warnings = listOf(
                "Do NOT stop CPR unless person recovers or help arrives",
                "Hard and fast is better than gentle CPR"
            )
        ),
        EmergencyProtocol(
            id = "choking",
            title = "Choking (Heimlich Maneuver)",
            icon = "🫁",
            severity = Severity.CRITICAL,
            callAmbulance = true,
            steps = listOf(
                "1. Ask 'Are you choking?' — if they can't speak/cough, act immediately",
                "2. STAND behind the person",
                "3. LEAN them slightly forward",
                "4. Give 5 firm back blows between shoulder blades with heel of hand",
                "5. Give 5 abdominal thrusts (Heimlich): hands above navel, thrust inward-upward",
                "6. ALTERNATE back blows and abdominal thrusts until object dislodges",
                "7. If unconscious — start CPR immediately",
                "8. For infants: face-down back blows + chest thrusts (NOT abdominal)"
            ),
            warnings = listOf(
                "Never do blind finger sweeps in mouth",
                "For pregnant women: use chest thrusts instead"
            )
        ),
        EmergencyProtocol(
            id = "severe_bleeding",
            title = "Severe Bleeding",
            icon = "🩸",
            severity = Severity.CRITICAL,
            callAmbulance = true,
            steps = listOf(
                "1. PROTECT yourself — use gloves or plastic bag if available",
                "2. APPLY direct pressure with clean cloth or bandage",
                "3. PRESS firmly and continuously — do NOT remove cloth",
                "4. ADD more cloth ON TOP if blood soaks through",
                "5. ELEVATE the wounded area above heart level if possible",
                "6. For limb bleeding: apply tourniquet 2 inches above wound if severe",
                "7. CALL 108 — keep pressure until help arrives",
                "8. Keep person warm and calm — watch for shock signs"
            ),
            warnings = listOf(
                "Do NOT remove embedded objects",
                "Shock signs: pale, cold, clammy skin + rapid weak pulse"
            )
        ),
        EmergencyProtocol(
            id = "stroke",
            title = "Stroke — FAST Protocol",
            icon = "🧠",
            severity = Severity.CRITICAL,
            callAmbulance = true,
            steps = listOf(
                "USE FAST method to identify stroke:",
                "F — FACE: Ask to smile. Does one side droop?",
                "A — ARMS: Ask to raise both arms. Does one drift down?",
                "S — SPEECH: Ask to repeat 'The sky is blue'. Is speech slurred?",
                "T — TIME: If ANY signs present, call 108 IMMEDIATELY",
                "WHILE WAITING:",
                "• Keep person calm and still",
                "• Do NOT give food, water or medication",
                "• Note exact time symptoms started",
                "• Lay on side if unconscious"
            ),
            warnings = listOf(
                "Time is critical — every minute counts in stroke",
                "Do NOT give aspirin unless doctor advises"
            )
        ),
        EmergencyProtocol(
            id = "burns",
            title = "Burns Treatment",
            icon = "🔥",
            severity = Severity.HIGH,
            steps = listOf(
                "MINOR BURNS (redness, no blisters):",
                "1. Cool with running cold water for 10-20 minutes",
                "2. Do NOT use ice, butter, or toothpaste",
                "3. Cover loosely with clean bandage",
                "4. Take paracetamol for pain",
                "SEVERE BURNS (blisters, charring, large area):",
                "1. Call 108 immediately",
                "2. Remove jewelry/clothes near burn (if not stuck)",
                "3. Cover with clean damp cloth",
                "4. Do NOT burst blisters"
            ),
            warnings = listOf(
                "Seek hospital for: burns on face/hands/genitals, burns >palm size",
                "Chemical burns: flush with water for 20+ minutes"
            )
        ),
        EmergencyProtocol(
            id = "fracture",
            title = "Fracture / Broken Bone",
            icon = "🦴",
            severity = Severity.HIGH,
            steps = listOf(
                "1. Do NOT try to straighten the bone",
                "2. IMMOBILIZE the injured area with a splint",
                "3. SPLINT: Use rigid material (wood, cardboard) + padding + bandage",
                "4. Apply ice pack (wrapped in cloth) to reduce swelling",
                "5. Elevate the injured limb if possible",
                "6. For open fracture (bone visible): cover with clean cloth, call 108",
                "7. Monitor pulse/feeling below the fracture",
                "8. Transport carefully to hospital"
            ),
            warnings = listOf(
                "NEVER move someone with suspected spine/neck fracture",
                "Open fractures have high infection risk"
            )
        ),
        EmergencyProtocol(
            id = "anaphylaxis",
            title = "Severe Allergic Reaction",
            icon = "⚠️",
            severity = Severity.CRITICAL,
            callAmbulance = true,
            steps = listOf(
                "Signs: hives, swelling, difficulty breathing, throat tightening, dizziness",
                "1. CALL 108 immediately",
                "2. Use Epinephrine auto-injector (EpiPen) if available — outer thigh",
                "3. Lay person flat with legs elevated (unless breathing difficulty)",
                "4. If unconscious and not breathing: start CPR",
                "5. Give second EpiPen dose after 5-15 minutes if no improvement",
                "6. Do NOT give antihistamines as sole treatment — not fast enough"
            ),
            warnings = listOf(
                "Anaphylaxis can be fatal within minutes",
                "Always go to hospital even after EpiPen — symptoms can return"
            )
        ),
        EmergencyProtocol(
            id = "heat_stroke",
            title = "Heat Stroke / Hyperthermia",
            icon = "☀️",
            severity = Severity.CRITICAL,
            callAmbulance = true,
            steps = listOf(
                "Signs: body temp >40°C, confusion, no sweating, red hot skin",
                "1. MOVE person to cool shaded area immediately",
                "2. CALL 108",
                "3. COOL rapidly: wet clothes with cold water, fan vigorously",
                "4. Apply ice packs to neck, armpits, groin",
                "5. If conscious: give cool water to sip slowly",
                "6. Do NOT give fever medications (paracetamol/aspirin)",
                "7. Continue cooling until temperature drops or help arrives"
            ),
            warnings = listOf(
                "Different from heat exhaustion — heat stroke is life-threatening",
                "Person may be confused/aggressive — keep cooling"
            )
        ),
        EmergencyProtocol(
            id = "drowning",
            title = "Drowning / Near-Drowning",
            icon = "🌊",
            severity = Severity.CRITICAL,
            callAmbulance = true,
            steps = listOf(
                "1. ENSURE YOUR SAFETY FIRST — reach/throw before entering water",
                "2. Get person out of water",
                "3. CHECK for breathing — tilt head back, look, listen, feel",
                "4. If not breathing: give 5 rescue breaths immediately",
                "5. Start CPR if no pulse — 30:2 ratio",
                "6. CALL 108",
                "7. All near-drowning victims need hospital evaluation",
                "8. Keep person warm — hypothermia risk"
            ),
            warnings = listOf(
                "Secondary drowning can occur hours later — always hospitalize",
                "Do NOT do Heimlich for drowning — do CPR"
            )
        )
    )

    val firstAidTopics = listOf(
        FirstAidTopic(
            id = "diabetes_emergency",
            title = "Diabetic Emergency",
            icon = "💉",
            category = "Medical Emergency",
            content = "Low blood sugar (hypoglycemia) is more immediately dangerous than high blood sugar.",
            steps = listOf(
                "LOW SUGAR signs: shaking, sweating, confusion, pale",
                "If conscious: give 15g sugar — 3 glucose tablets, 150ml juice, 3 tsp sugar",
                "Recheck in 15 mins, repeat if still symptomatic",
                "If unconscious: DO NOT give food/drink — call 108",
                "HIGH SUGAR signs: frequent urination, extreme thirst, fruity breath",
                "High sugar: give water, seek medical care — not usually immediate emergency"
            )
        ),
        FirstAidTopic(
            id = "snake_bite",
            title = "Snake Bite",
            icon = "🐍",
            category = "Animal Emergency",
            content = "In India, 4 species cause 95% of snakebite deaths (Big Four).",
            steps = listOf(
                "1. Keep victim CALM and STILL — movement spreads venom",
                "2. CALL 108 immediately",
                "3. Remove jewelry/tight clothing near bite",
                "4. Mark the bite with pen + note time",
                "5. Immobilize bitten limb below heart level",
                "6. Transport to hospital — anti-venom needed",
                "DO NOT: suck venom, cut and drain, apply tourniquet, use ice",
                "DO NOT: give alcohol or traditional medicines"
            )
        ),
        FirstAidTopic(
            id = "road_accident",
            title = "Road Accident Triage",
            icon = "🚗",
            category = "Trauma",
            content = "The Good Samaritans law (India 2015) protects bystanders who help accident victims.",
            steps = listOf(
                "1. Ensure scene is safe — turn off ignition, hazard lights on",
                "2. CALL 108 with location",
                "3. Do NOT move victim unless in immediate danger (fire)",
                "4. Check consciousness and breathing",
                "5. Control bleeding with direct pressure",
                "6. Keep victim warm and reassured",
                "7. Assume spinal injury — keep head/neck stable",
                "8. You CANNOT be held legally liable for helping in India"
            )
        ),
        FirstAidTopic(
            id = "seizure",
            title = "Seizure / Epilepsy",
            icon = "⚡",
            category = "Neurological",
            content = "Most seizures stop on their own within 2-3 minutes.",
            steps = listOf(
                "1. PROTECT from injury — clear the area",
                "2. Cushion the head with something soft",
                "3. Roll on their side to prevent choking",
                "4. Do NOT restrain — do NOT put anything in mouth",
                "5. Time the seizure",
                "CALL 108 if: first seizure, lasts >5 mins, no recovery, injury occurred",
                "After seizure: person may be confused — stay calm and reassure them",
                "Recovery position: on side, airway clear"
            )
        ),
        FirstAidTopic(
            id = "eye_injury",
            title = "Eye Injury / Chemical Splash",
            icon = "👁️",
            category = "Trauma",
            content = "Eye injuries require prompt treatment to prevent permanent damage.",
            steps = listOf(
                "CHEMICAL SPLASH:",
                "1. Flush with clean water for 20+ minutes continuously",
                "2. Remove contact lenses if present",
                "3. Go to hospital immediately",
                "FOREIGN BODY:",
                "1. Do NOT rub the eye",
                "2. Flush with clean water",
                "3. If object visible on white of eye, try to flush out",
                "4. If embedded: cover both eyes and go to hospital"
            )
        )
    )

    val commonDrugs = listOf(
        DrugInfo(
            name = "Paracetamol (Crocin, Dolo)",
            genericName = "Acetaminophen",
            uses = "Fever, mild to moderate pain, headache, body ache",
            dosageAdult = "500-1000mg every 4-6 hours. Max 4g/day",
            dosageChild = "10-15mg/kg every 4-6 hours. Max 5 doses/day",
            warnings = "Do NOT exceed recommended dose. Avoid with alcohol or liver disease.",
            sideEffects = "Generally well tolerated. Overdose can cause liver damage.",
            interactions = "Warfarin (blood thinners), alcohol"
        ),
        DrugInfo(
            name = "ORS (Oral Rehydration Salts)",
            genericName = "Electrolyte solution",
            uses = "Diarrhea, dehydration, vomiting, heat exhaustion",
            dosageAdult = "200-400ml after each loose stool. Make fresh every 24 hours.",
            dosageChild = "50-100ml after each loose stool for <2 years. 100-200ml for older.",
            warnings = "Use clean water. Discard after 24 hours.",
            sideEffects = "None if prepared correctly",
            interactions = "None significant"
        ),
        DrugInfo(
            name = "Ibuprofen (Brufen, Advil)",
            genericName = "Ibuprofen",
            uses = "Pain, inflammation, fever, menstrual cramps",
            dosageAdult = "200-400mg every 4-6 hours with food. Max 1200mg/day (OTC)",
            dosageChild = "5-10mg/kg every 6-8 hours. Not for infants <6 months.",
            warnings = "Take with food. Avoid with kidney disease, stomach ulcers, heart conditions. Avoid in last trimester of pregnancy.",
            sideEffects = "Stomach upset, nausea. Rare: stomach bleeding.",
            interactions = "Blood thinners, aspirin, ACE inhibitors"
        ),
        DrugInfo(
            name = "Antacid (Gelusil, Digene)",
            genericName = "Aluminium/Magnesium hydroxide",
            uses = "Heartburn, acidity, indigestion, gastric pain",
            dosageAdult = "1-2 tablets or 10-20ml 1 hour after meals and at bedtime",
            dosageChild = "Consult doctor",
            warnings = "Do not take within 2 hours of other medications.",
            sideEffects = "Constipation (aluminium) or diarrhea (magnesium)",
            interactions = "Antibiotics, thyroid medications — take 2 hours apart"
        ),
        DrugInfo(
            name = "Cetirizine (Cetzine, Alerid)",
            genericName = "Cetirizine hydrochloride",
            uses = "Allergies, hay fever, hives, runny nose, itching",
            dosageAdult = "10mg once daily",
            dosageChild = "5mg (2.5ml syrup) for 2-6 years. 10mg for >6 years.",
            warnings = "May cause drowsiness. Avoid driving. Avoid alcohol.",
            sideEffects = "Drowsiness, dry mouth, headache",
            interactions = "Alcohol, other sedatives"
        )
    )

    val symptomCategories = mapOf(
        "Chest & Heart" to listOf("Chest pain", "Palpitations", "Shortness of breath", "Arm/jaw pain"),
        "Head & Brain" to listOf("Severe headache", "Confusion", "Slurred speech", "Loss of consciousness"),
        "Breathing" to listOf("Difficulty breathing", "Wheezing", "Blue lips", "Coughing blood"),
        "Abdomen" to listOf("Severe stomach pain", "Vomiting blood", "Rigid abdomen", "Diarrhea"),
        "Allergic" to listOf("Rash/hives", "Throat swelling", "Facial swelling", "Dizziness after food"),
        "Trauma" to listOf("Head injury", "Bone fracture", "Deep wound", "Eye injury"),
        "Fever" to listOf("High fever >104°F", "Stiff neck + fever", "Rash + fever", "Fits with fever")
    )
}