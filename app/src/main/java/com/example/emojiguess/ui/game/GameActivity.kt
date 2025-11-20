package com.example.emojiguess.ui.game

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.emojiguess.MainActivity
import com.example.emojiguess.R
import com.example.emojiguess.Screen
import com.example.emojiguess.databinding.ActivityGameBinding
import com.example.emojiguess.ui.chat.ChatFragment
import com.example.emojiguess.utils.Constants
import com.google.android.material.chip.Chip
import com.google.android.material.snackbar.Snackbar

class GameActivity : AppCompatActivity() {

    private lateinit var binding: ActivityGameBinding
    private val viewModel: GameViewModel by viewModels()
    private val playerAdapter = PlayerAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGameBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val roomId = intent.getStringExtra(EXTRA_ROOM_ID) ?: DEFAULT_ROOM
        val playerId = intent.getStringExtra(EXTRA_PLAYER_ID) ?: DEFAULT_PLAYER_ID
        val playerName = intent.getStringExtra(EXTRA_PLAYER_NAME) ?: DEFAULT_PLAYER_NAME

        viewModel.initialize(roomId, playerId, playerName)
        setupToolbar(roomId)
        setupPlayersList()
        observeViewModel()
        setupActions()
        attachChatFragment(roomId, playerId, playerName)
    }

    private fun setupToolbar(roomId: String) {
        binding.textRoomTitle.text = if (roomId.isNotEmpty()) {
            "Sala: $roomId"
        } else {
            "Sala: Cargando..."
        }
    }

    private fun setupPlayersList() {
        binding.recyclerPlayers.apply {
            layoutManager = LinearLayoutManager(this@GameActivity)
            adapter = playerAdapter
        }
    }

    private fun observeViewModel() {
        viewModel.players.observe(this) { players ->
            playerAdapter.submitList(players)
        }
        viewModel.timerSeconds.observe(this) { seconds ->
            binding.textTimer.text = "${seconds}s"
            // Cambiar color si queda poco tiempo
            if (seconds <= Constants.DEFAULT_ROUND_DURATION / 3) {
                binding.textTimer.setTextColor(getColor(R.color.playerEliminated))
            } else {
                binding.textTimer.setTextColor(getColor(R.color.colorPrimary))
            }
        }
        viewModel.hiddenEmoji.observe(this) { emoji ->
            binding.textHiddenEmoji.text = emoji
        }
        viewModel.availableEmojis.observe(this, emojiObserver)
        viewModel.statusMessage.observe(this) { status ->
            binding.textStatus.text = status
        }
        viewModel.isConfirmEnabled.observe(this) { enabled ->
            binding.buttonConfirm.isEnabled = enabled
        }
        viewModel.isPlayerTurn.observe(this) { isTurn ->
            binding.textTurnIndicator.isVisible = isTurn
        }
        viewModel.selectedEmoji.observe(this) { selected ->
            updateChipSelection(selected)
        }
        viewModel.gameFinished.observe(this) { result ->
            result?.let { 
                // Esperar un momento antes de navegar
                binding.root.postDelayed({
                    navigateToEndScreen(it.won, it.winnerName)
                }, Constants.ROUND_END_DELAY)
            }
        }
    }
    
    private fun navigateToEndScreen(won: Boolean, winnerName: String) {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            
            val route = if (won) {
                "${Screen.Victory.route}/$winnerName"
            } else {
                "${Screen.Defeat.route}/$winnerName"
            }
            
            putExtra("navigation_route", route)
        }
        startActivity(intent)
        finish()
    }

    private val emojiObserver = Observer<List<String>> { emojis ->
        val selected = viewModel.selectedEmoji.value
        binding.chipGroupEmojis.removeAllViews()
        val inflater = layoutInflater
        emojis.forEach { emoji ->
            val chip = inflater.inflate(R.layout.view_emoji_chip, binding.chipGroupEmojis, false) as Chip
            chip.text = emoji
            chip.isCheckable = true
            chip.isChecked = emoji == selected
            chip.setOnClickListener { viewModel.onEmojiSelected(emoji) }
            binding.chipGroupEmojis.addView(chip)
        }
    }

    private fun updateChipSelection(selectedEmoji: String?) {
        for (index in 0 until binding.chipGroupEmojis.childCount) {
            val chip = binding.chipGroupEmojis.getChildAt(index)
            if (chip is Chip) {
                chip.isChecked = chip.text?.toString() == selectedEmoji
            }
        }
    }

    private fun setupActions() {
        binding.buttonConfirm.setOnClickListener {
            if (viewModel.isPlayerTurn.value != true) {
                Snackbar.make(binding.root, R.string.error_not_your_turn, Snackbar.LENGTH_SHORT).show()
            } else {
                viewModel.confirmSelection()
            }
        }
    }

    private fun attachChatFragment(roomId: String, playerId: String, playerName: String) {
        val containerId = binding.chatContainer.id
        val existing = supportFragmentManager.findFragmentById(containerId)
        if (existing == null) {
            supportFragmentManager.beginTransaction()
                .replace(containerId, ChatFragment.newInstance(roomId, playerId, playerName))
                .commit()
        }
    }

    companion object {
        private const val EXTRA_ROOM_ID = "extra_room_id"
        private const val EXTRA_PLAYER_ID = "extra_player_id"
        private const val EXTRA_PLAYER_NAME = "extra_player_name"

        private const val DEFAULT_ROOM = "demo-room"
        private const val DEFAULT_PLAYER_ID = "player-1"
        private const val DEFAULT_PLAYER_NAME = "Invitado"

        fun createIntent(context: Context, roomId: String, playerId: String, playerName: String): Intent {
            return Intent(context, GameActivity::class.java).apply {
                putExtra(EXTRA_ROOM_ID, roomId)
                putExtra(EXTRA_PLAYER_ID, playerId)
                putExtra(EXTRA_PLAYER_NAME, playerName)
            }
        }
    }
}