#!/bin/bash
# Percorre o fluxo do protótipo no emulador e salva prints numerados em screenshots/.
# Requisitos: emulador ligado com o app instalado (./gradlew installDebug), adb do Android SDK e Python com Pillow.
# Uso (Git Bash ou Linux/macOS): bash scripts/capturar-prints.sh
# Os toques usam o texto/descrição dos elementos; o arraste de desbloqueio e as faixas etárias usam coordenadas de uma tela 1080x2400.
export MSYS_NO_PATHCONV=1
ADB="${ADB:-adb}"
PYTHON="${PYTHON:-python3}"  # no Windows, use PYTHON=python
OUT="$(cd "$(dirname "$0")/.." && pwd)/screenshots"
mkdir -p "$OUT"
dump() { "$ADB" shell uiautomator dump /sdcard/ui.xml >/dev/null 2>&1; "$ADB" shell cat /sdcard/ui.xml; }
tapText() {
  local b; b=$(dump | grep -oE "(text|content-desc)=\"$1\"[^>]*bounds=\"[^\"]*\"" | head -1 | grep -o 'bounds="[^"]*"' | grep -o '[0-9]*' | tr '\n' ' ')
  if [ -z "$b" ]; then echo "NOT FOUND: $1"; return 1; fi
  set -- $b; "$ADB" shell input tap $(( ($1+$3)/2 )) $(( ($2+$4)/2 )); sleep 1.2
}
shot() {
  sleep 1; "$ADB" exec-out screencap -p > "$OUT/$1.full.png"
  "$PYTHON" -I -c "from PIL import Image;import sys;im=Image.open(sys.argv[1]);im.resize((im.width//2,im.height//2),Image.LANCZOS).save(sys.argv[2],optimize=True)" "$OUT/$1.full.png" "$OUT/$1.png"
  rm "$OUT/$1.full.png"; echo "shot $1"
}
dismissBanner() { dump | grep -q 'text="Got it"' && tapText "Got it"; true; }  # aviso de tela cheia do Android

"$ADB" shell pm clear com.example.naomordaaisca >/dev/null
"$ADB" shell am start -n com.example.naomordaaisca/.MainActivity >/dev/null; sleep 3; dismissBanner
shot 01-inicio
"$ADB" shell input tap 780 712; sleep 1.2; shot 02-inicio-faixa-agente
"$ADB" shell input tap 270 712; sleep 1.2
tapText "Pegar o celular"; shot 03-tela-de-bloqueio
"$ADB" shell input swipe 100 1930 1000 1930 600; sleep 1.2; shot 04-tela-inicial
tapText "Galeria"; shot 05-app-sem-pistas
"$ADB" shell input keyevent KEYCODE_BACK; sleep 1
tapText "Send"; shot 06-send-conversas
tapText "Lia"; shot 07-send-conversa-lia
tapText "O que a página pede"; shot 08-pista-examinada
tapText "Anotar como pista"; shot 09-pista-anotada
"$ADB" shell input keyevent KEYCODE_BACK; sleep 1; "$ADB" shell input keyevent KEYCODE_BACK; sleep 1
tapText "Navegador"; shot 10-navegador
tapText "Notícias do jogo"; tapText "Anotar como pista"; shot 11-navegador-pista-anotada
tapText "Voltar"; shot 12-escolhas-liberadas
tapText "Digitar a senha e o código para tentar pegar a skin"; shot 13-consequencia
tapText "Continuar"; shot 14-recuperacao
tapText "Fechei a página e pedi ajuda"; shot 15-investigacao-concluida
tapText "Reiniciar investigação"; shot 16-confirmar-reinicio
tapText "Reiniciar"; sleep 1
"$ADB" shell input swipe 100 1930 1000 1930 600; sleep 1.2
tapText "Mais opções"; shot 17-menu-opcoes
tapText "Sair da investigação"; shot 18-confirmar-saida
tapText "Sair"; shot 19-inicio-com-progresso
tapText "A mensagem da escola"; shot 20-missao-antiga
"$ADB" shell input keyevent KEYCODE_BACK; sleep 1
echo done
