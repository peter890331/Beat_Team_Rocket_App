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

#### 📋 啟動前檢查清單 (Preparation Check List)
Before starting the script, please make sure you have prepared the following in your device:

- **【GPS JoyStick 設定 / Settings】**
    - 傳送：建議將操作桿預設為「隱藏」。     
        *Teleport: It is recommended to set the joystick to "Hidden" by default.*
- **【Pokémon GO 準備 / Preparation】**
    - 孵蛋：建議暫停孵「蛋」。      
        *Egg Hatching: It is recommended to pause egg hatching.*
    - 大佬：建議解除裝備「火箭隊雷達」與「超級火箭隊雷達」。      
        *Radars: It is recommended to unequip the "Rocket Radar" and "Super Rocket Radar".*
    - 夥伴：建議攜帶達到「給力好夥伴」等級以上的寶可夢作為夥伴。      
        *Buddy: It is recommended to have a buddy Pokémon at the "Great Buddy" level or higher (for Catch Assist).*
    - 物資：確保背包備有足夠的「厲害傷藥」與「活力碎片」，可以事先刷路線獲得。      
        *Supplies: Ensure your Item Bag has enough "Hyper Potions" and "Revives". You can farm Routes beforehand to get them.*
    - 打手：確保在火箭隊對戰中皆已預先編排好各屬性對應的「小隊」。      
        *Battle Parties: Ensure that you have pre-arranged corresponding battle "Parties" for each type in Team GO Rocket battles.*
- **【注意事項 / Precautions】**
    - 提醒：此腳本執行時建議全程在旁觀看，以便隨時應對突發狀況。      
        *Reminder: It is recommended to keep an eye on the screen while the script is running to handle any unexpected situations.*
    - 雷達：此腳本的座標來源是 PokeList，有時會出現查無座標的情況。      
        *Radar Source: The coordinate source for this script is PokeList; sometimes there may be situations where no coordinates are available.*

---

## How to use, For users
For users, you can directly download the final apk version in Releases and install it on your Android phone.

### Steps:
1. Download the `Beat_Team_Rocket_App.apk` file in Releases and install it on your device.
2. Open the app. The system will prompt you to grant the "Display over other apps" and "Accessibility Service" permissions.
3. After granting permissions, you are ready to start using the script.

   #### Home page
   First will be taken to the Home page, the descriptions in the main window as follows:

    - **火箭隊助手 - Beat Team Rocket App, made by Peter Yu.**

    - **參數設定 (Parameter Settings)**   
      在開始執行腳本之前，請在此處設定你的參數。    
      *Before start running the script, please set your parameters here.*

      The descriptions of settings as follows:
        
        - **起始座標 (Start Coordinate)**：  
          腳本啟動時的初始位置基準點。  
          *The initial position base point when the script starts.*
          > Enter your desired starting coordinate (Latitude, Longitude) in 起始座標, ex: 25.032966, 121.535516.

        - **雷達掃描週期 (Radar scan limit)**：  
          前往幾個座標後，要重新抓取一次網頁雷達資料。（限整數，預設為5）  
          *After visiting how many coordinates, fetch the web radar data again. (Limit to an integer, default is 5)*
          > Type in the input box for "雷達掃描週期".

        - **復活補血週期 (Heal limit)**：  
          打完幾個手下後，要開啟背包執行一次復活與補血流程。（限整數，預設為5）  
          *After defeating how many grunts, open the bag to execute the revive and heal process. (Limit to an integer, default is 5)*
          > Type in the input box for "復活補血週期".

    - **目標屬性 (Target Types)**：  
      選擇你想要挑戰的火箭隊屬性。  
      *Select the attributes of the Team Rocket grunts you want to challenge.*
      > Click the buttons below to select or unselect. You can choose specific types, or click "全選 (Select All)" to fight all available grunts.

    - **啟動腳本 (Start Script)**：  
      確認完成後，直接點擊下方「啟動」！  
      *After confirming settings are complete, click below to start!*

4. After clicking Start, the app will request Screen Capture permission (used for OpenCV image recognition), just allow it.
5. Then both GPS JoyStick and Pokémon GO will launch automatically, and a floating window will appear on your screen.

   #### Floating Control Panel
   The descriptions of the floating panel as follows:

    - **主圖示 (Main Icon)**：  
      長按可上下拖曳懸浮窗位置。  
      *Long press to drag the floating window up and down.*

    - **▶ 啟動 (Play)**：  
      確認遊戲載入完畢，並將視角拉至最小後，**必須點擊此按鈕才會正式開始自動化掛機流程**。  
      *Make sure the game is fully loaded and the perspective is minimized, then **you must click this button to officially start the botting process**.*

    - **✖ 關閉 (Close)**：  
      強制停止腳本運行並關閉懸浮窗。  
      *Force stop the script running and close the floating window.*

    - **0 / 0 計數 (Counter)**：  
      顯示當前進度（成功捕捉數量 / 總拜訪補給站數量）。  
      *Shows current progress (Successful catch count / Total visited PokéStops count).*

    - **🔆 / 🌕 距離感應模式 (Proximity Sensor Mode)**：  
      防誤觸省電開關。啟用後，只要遮擋手機上方的距離感測器（例如放入口袋或倒扣桌面），螢幕就會完全熄滅以達到省電與防誤觸的效果，同時腳本仍會在背景持續運行。移開遮擋物後，螢幕即會重新亮起。  
      *Anti-touch power-saving switch. When enabled, covering the phone's top proximity sensor (such as putting it in your pocket or placing it face down) will turn off the screen completely to save power and prevent accidental touches, while the script continues running in the background. Remove the cover, and the screen will light up again.*

    - **📸 截圖 (Screenshot)**：  
      手動截取當前畫面並儲存。點擊此按鈕會自動以 PNG 格式儲存到手機內的 Pictures/PokemonGO 資料夾中。  
      *Manually capture the current screen and save it. The image will be automatically saved as a PNG file in your phone's Pictures/PokemonGO folder.*

4. Enjoy the convenience brought by this bot 🤓.

---

## Some examples when using
### screen record

---

## Notes
**【相容性聲明 / Compatibility Note】** 

這個 App 雖然有意要做成不同手機通用的版本，但無奈我沒有更多手機進行測試。因此，目前所有的影像辨識模板都是以我的手機螢幕為準。我不確定它是否能在不同解析度或比例的手機上正常運作。

*Although this app is intended to be universally compatible with different Android devices, I unfortunately don't have other phones to test it on. As a result, all current image recognition templates are calibrated exclusively to my own device's screen. I cannot guarantee that it will work properly on phones with different resolutions or aspect ratios.*

以前的電腦版 (Previous PC Version)：  
🔗 [Beat_Team_Rocket_cmd_version](https://github.com/peter890331/Beat_Team_Rocket_cmd_version)

---

### ❗ 警告：僅以此練習程式編寫，請勿在遊戲中使用外掛，否則後果自負！本人對此內容不負任何法律責任。 ❗
### ❗ WARNING: Practice programming only, please do not use it to cheat on the game!
### The consequences are your own! I will not be responsible for any law liability to this content. ❗