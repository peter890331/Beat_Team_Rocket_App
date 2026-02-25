# Beat_Team_Rocket_App
Beat_Team_Rocket_App, made by Peter Yu.

> 一個在Pokémon GO中自動打火箭隊手下並捕捉暗影寶可夢的外掛腳本。    
> A game bot script that automatically defeats Team GO Rocket Grunts and catches Shadow Pokémon in Pokémon GO.
>
> I am a Taiwanese, so some instructions and description of parameters in the program are described in traditional Chinese, Taiwan No.1!!!!!

### ❗ 警告：僅以此練習程式編寫，請勿在遊戲中使用外掛，否則後果自負！本人對此內容不負任何法律責任。 ❗
### ❗ WARNING: Practice programming only, please do not use it to cheat on the game!
### The consequences are your own! I will not be responsible for any law liability to this content. ❗
<img src="pokemon_go_icon.png" width="300px" alt="pokemon_go_icon.png">

---

## Foreword
In Pokémon Go, Team GO Rocket grunts invade PokéStops and challenge players to battles, where defeating them allows players to rescue Shadow Pokémon, which inherently possess a unique attack bonus that makes them extremely powerful in battles just as they are, and furthermore, can be purified to significantly increase their IVs, providing a much higher chance to obtain an iv100 (perfect IV) Pokémon. However, farming Team GO Rocket requires players to constantly spin PokéStops, engage in repetitive tapping battles, and manually catch the Pokémon. You can see how tedious and time-consuming it is to manually farm a large number of Shadow Pokémons to get a perfect or powerful one.

---

## Overview
This game bot application can fully automatically send your game position to the nearest invaded PokéStops based on your selected target types, click the stop, engage in battle with the Team GO Rocket grunt using your pre-set battle parties, and automatically catch the Shadow Pokémon after winning. It even supports automatic healing and reviving!

Equipment and Software Requirements:
1. A rooted Android phone with GPS JoyStick ([Google Play][1]) installed **IN THE SYSTEM**, and of course, install Pokémon GO.

[1]: https://play.google.com/store/apps/details?id=com.theappninjas.fakegpsjoystick

---

## How to use, For users
For users, you can directly download the final apk version in Releases and install it on your Android phone.
### Steps:
1. Download the `Beat_Team_Rocket_App.apk` file in Releases and install it on your device.
2. Open the app. The system will prompt you to grant the "Display over other apps" and "Accessibility Service" permissions.
3. After granting permissions, you are ready to start using the script.

   #### Home page
   First will be taken to the Home page, the descriptions in the main window as follows:

    - 火箭隊助手 - Beat Team Rocket App, made by Peter Yu.**

    - 參數設定 (Parameter Settings)** &nbsp;&nbsp;&nbsp;*Before start running the script, please set your parameters here.*

      The descriptions of settings as follows:
        - 起始座標 (Start Coordinate)：  
          腳本啟動時的初始位置基準點。  
          *The initial position base point when the script starts.*
          > Enter your desired starting coordinate (Latitude, Longitude) in 起始座標, ex: 25.032966, 121.535516.

        - 雷達掃描週期 (Radar scan limit)：  
          前往幾個座標後，要重新抓取一次網頁雷達資料。（限整數，預設為5）  
          *After visiting how many coordinates, fetch the web radar data again. (Limit to an integer, default is 5)*
          > Type in the input box for "雷達掃描週期".

        - 復活補血週期 (Heal limit)：  
          打完幾個手下後，要開啟背包執行一次復活與補血流程。（限整數，預設為5）  
          *After defeating how many grunts, open the bag to execute the revive and heal process. (Limit to an integer, default is 5)*
          > Type in the input box for "復活補血週期".

    - 目標屬性 (Target Types)**：  
      選擇你想要挑戰的火箭隊屬性。  
      *Select the attributes of the Team Rocket grunts you want to challenge.*
      > Click the buttons below to select or unselect. You can choose specific types, or click "全選 (Select All)" to fight all available grunts.

    - 啟動腳本 (Start Script)**：  
      確認完成後，直接點擊下方「啟動」！  
      *After confirming settings are complete, click below to start!*

   ---

   #### Floating Control Panel
   After clicking Start, the app will request Screen Capture permission (used for OpenCV image recognition), just allow it. Then both GPS JoyStick and Pokémon GO will launch automatically, and a floating window will appear on your screen.

   The descriptions of the floating panel as follows:

    - 主圖示 (Main Icon)**：  
      長按可上下拖曳懸浮窗位置。  
      *Long press to drag the floating window up and down.*

    - ▶ 啟動 (Play)**：  
      確認遊戲載入完畢，並將視角拉至最小後，**必須點擊此按鈕才會正式開始自動化掛機流程**。  
      *Make sure the game is fully loaded and the perspective is minimized, then **you must click this button to officially start the botting process**.*

    - ✖ 關閉 (Close)**：  
      強制停止腳本運行並關閉懸浮窗。  
      *Force stop the script running and close the floating window.*

    - 0 / 0 計數 (Counter)**：  
      顯示當前進度（成功捕捉數量 / 總拜訪補給站數量）。  
      *Shows current progress (Successful catch count / Total visited PokéStops count).*

    - 🔆 / 🌕 距離感應模式 (Proximity Sensor Mode)**：  
      防誤觸省電開關。啟用後，只要遮擋手機上方的距離感測器（例如放入口袋或倒扣桌面），螢幕就會完全熄滅以達到省電與防誤觸的效果，同時腳本仍會在背景持續運行。移開遮擋物後，螢幕即會重新亮起。  
      *Anti-touch power-saving switch. When enabled, covering the phone's top proximity sensor (such as putting it in your pocket or placing it face down) will turn off the screen completely to save power and prevent accidental touches, while the script continues running in the background. Remove the cover, and the screen will light up again.*

    - 📸 截圖 (Screenshot)**：  
      手動截取當前畫面並儲存。點擊此按鈕會自動以 PNG 格式儲存到手機內的 `Pictures/PokemonGO` 資料夾中。  
      *Manually capture the current screen and save it. The image will be automatically saved as a PNG file in your phone's `Pictures/PokemonGO` folder.*
---

### ❗ 警告：僅以此練習程式編寫，請勿在遊戲中使用外掛，否則後果自負！本人對此內容不負任何法律責任。 ❗
### ❗ WARNING: Practice programming only, please do not use it to cheat on the game!
### The consequences are your own! I will not be responsible for any law liability to this content. ❗

