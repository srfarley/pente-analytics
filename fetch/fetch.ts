import * as fs from 'fs';
import axios from 'axios'
import { AxiosResponse } from 'axios'

const [username, password, total] = process.argv.slice(2, 5)

loginFetchSave(username, password, Number.parseInt(total) || 100)

async function loginFetchSave(username, password, total): Promise<void[]> {
  const gamesPerArchive = 100;
  const cookieHeaders = await login(username, password)
  let iterations = Math.ceil(total / gamesPerArchive)
  console.log(`Downloading ${iterations * gamesPerArchive} games from pente.org for user ${username}...`)
  const savePromises = new Array<Promise<void>>()
  for (let i = 0; i < iterations; i++) {
    const startGameIndex = i * gamesPerArchive;
    const iterCount = gamesPerArchive
    const savePromise = fetchAndSave(cookieHeaders, username, '', startGameIndex, iterCount)
    savePromises.push(savePromise)
  }
  return await Promise.all(savePromises)
}

async function login(username: string, password: string): Promise<string[]> {
  const loginUrl = 'https://www.pente.org/gameServer/index.jsp'
  var params = new URLSearchParams()
  params.append('name2', username)
  params.append('password2', password)
  const response = await axios.post(loginUrl, params)
  return response.headers['set-cookie'] as string[];
}

async function fetchAndSave(
    cookieHeaders: string[],
    player1Username: string,
    player2Username: string,
    startGameIndex: number,
    numGames: number): Promise<void> {
  const response = await fetch(cookieHeaders, player1Username, player2Username, startGameIndex, numGames)
  player1Username = player1Username.length === 0 ? 'ALL' : player1Username
  player2Username = player2Username.length === 0 ? 'ALL' : player2Username
  const startNum = startGameIndex + 1;
  const endNum = startNum + numGames - 1
  const fileName = `pente_${player1Username}-${player2Username}-${startNum}-${endNum}.zip`
  console.log(`Downloaded games ${startNum} to ${endNum}`)
  return fs.promises.writeFile(fileName, response.data)
}

async function fetch(
    cookieHeaders: string[],
    player1Username: string,
    player2Username: string,
    startGameNumber: number,
    numGames: number): Promise<AxiosResponse<any>>{
  const endGameNumber = startGameNumber + numGames
  const searchUrl = 'https://pente.org/gameServer/controller/search.zip'
  const formatData = `moves=K10%2C&response_format=org.pente.gameDatabase.ZipFileGameStorerSearchResponseStream&response_params=zippedPartNumParam%3D1&results_order=1&filter_data=start_game_num%3D${startGameNumber}%26end_game_num%3D${endGameNumber}%26player_1_name%3D${player1Username}%26player_2_name%3D${player2Username}%26game%3DPente%26site%3DAll%2520Sites%26event%3DAll%2520Events%26round%3DAll%2520Rounds%26section%3DAll%2520Sections%26winner%3D0%26exclude_timeout%3Dfalse%26p1_or_p2%3Dfalse`

  const params = new URLSearchParams()
  params.append('format_name', 'org.pente.gameDatabase.SimpleGameStorerSearchRequestFormat')
  params.append('format_data', formatData)

  return await axios.post(searchUrl, params, {
    headers: { 'Cookie': cookieHeaders },
    responseType: 'arraybuffer'
  })
}
